package org.rm3l.sdr_issue_map_deserialization;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rm3l.sdr_issue_map_deserialization.domain.Item;
import org.rm3l.sdr_issue_map_deserialization.domain.Order;
import org.rm3l.sdr_issue_map_deserialization.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(
    properties = {
        "datasource.persistence-unit=MyTestPersistenceUnit",
        "spring.datasource.url=jdbc:h2:mem:test_db;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password="
    }
)
class SdrIssueMapDeserializationApplicationTests {

  private static final Logger logger =
      LoggerFactory.getLogger(SdrIssueMapDeserializationApplicationTests.class);

  private MockMvc mvc;

  @Autowired private WebApplicationContext context;

  @Autowired private OrderRepository orderRepository;

  @BeforeEach
  void beforeEachTestMethod() {
    this.mvc = MockMvcBuilders.webAppContextSetup(this.context)
        .alwaysDo(
            result -> {
              if (logger.isTraceEnabled()) {
                final MockHttpServletRequest request = result.getRequest();
                final MockHttpServletResponse response = result.getResponse();
                logger.trace("############### START #######################");
                logger.trace(">>> Request : {} {}",
                    request.getMethod(),
                    request.getRequestURI());
                logger.trace(
                    "<<< Response : [{}]\n{}",
                    response.getStatus(),
                    response.getContentAsString());
                logger.trace("################ END ###########################\n");
              }
            })
        .build();
  }

  @Test
  public void testResourceWithMap() throws Exception {

    //Fetch an existing resource available from the data store
    Order order = new Order();
    order.setInternalId(9999L);
    order.setName("my test order");
    final Item item1 = new Item();
    item1.setItemName("my test order item");
    item1.setMyIntegerAttribute(333);
    order.setItemMap(Map.of("first", item1));
    order = orderRepository.saveAndFlush(order);
    final Long existingOrderIdDb = order.getIdDb();
    Assertions.assertNotNull(existingOrderIdDb);
    this.mvc
        .perform(
            get(String.format("/Order/%d", existingOrderIdDb))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.idDb", is(existingOrderIdDb.intValue())))
        .andExpect(jsonPath("$.name", is("my test order")))
        .andExpect(jsonPath("$.internalId", is(9999)))
        .andExpect(jsonPath("$._links.itemMap.href", notNullValue()));
    this.mvc
        .perform(
            get(String.format(
                "/Order/%d/itemMap", existingOrderIdDb))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.first.idDb", notNullValue()))
        .andExpect(jsonPath("$.content.first.itemName", is("my test order item")))
        .andExpect(jsonPath("$.content.first.myIntegerAttribute", is(333)));


    // Create a new resource via a POST request
    String resourceToPost =
        "{"
            + "\"name\" : \"my order #1\","
            + "\"my_unknown_property\" : \"value for my unknown property\","
            + "\"my_unknown_property2\" : true,"
            + "\"internalId\" : 1000,"
            + "\"ItemMap\" : {"
            + "\"key_1\" : {\"itemName\" : \"my item #1\", \"myIntegerAttribute\": 11, \"fake_attr\" : \"does not exist\"}"
            + ","
            + "\"key_2\" : {\"itemName\" : \"my item #2\", \"myIntegerAttribute\": 12}"
            + "}" // itemMap
            + "}";
    final MockHttpServletResponse postResponse =
        this.mvc
            .perform(
                post("/Order")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(resourceToPost))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.idDb", notNullValue()))
            .andExpect(jsonPath("$.name", is("my order #1")))
            .andExpect(jsonPath("$.internalId", is(1000)))
            .andExpect(jsonPath("$.my_unknown_property").doesNotExist())
            .andExpect(jsonPath("$.my_unknown_property2").doesNotExist())
            .andExpect(jsonPath("$._links.itemMap.href", notNullValue()))
            .andReturn()
            .getResponse();
    final int myCompoundEntityIdDb =
        JsonPath.<Integer>read(postResponse.getContentAsString(), "$.idDb");

    // Make sure we can retrieve elements of the map
    this.mvc
        .perform(
            get(String.format(
                "/Order/%d/itemMap", myCompoundEntityIdDb))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.key_1.idDb", notNullValue()))
        .andExpect(jsonPath("$.content.key_1.itemName", is("my item #1")))
        .andExpect(jsonPath("$.content.key_1.myIntegerAttribute", is(11)))
        .andExpect(jsonPath("$.content.key_1.fake_attr").doesNotExist())
        .andExpect(jsonPath("$.content.key_2.idDb", notNullValue()))
        .andExpect(jsonPath("$.content.key_2.itemName", is("my item #2")))
        .andExpect(jsonPath("$.content.key_2.myIntegerAttribute", is(12)));
  }

}
