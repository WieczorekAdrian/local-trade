package io.github.adrian.wieczorek.local_trade.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementEntity;
import io.github.adrian.wieczorek.local_trade.service.category.CategoryEntity;
import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementRepository;
import io.github.adrian.wieczorek.local_trade.service.category.CategoryRepository;
import io.github.adrian.wieczorek.local_trade.service.user.UsersRepository;
import io.github.adrian.wieczorek.local_trade.testutils.AdFiltersUtils;
import io.github.adrian.wieczorek.local_trade.testutils.AdUtils;
import io.github.adrian.wieczorek.local_trade.testutils.CategoryUtils;
import io.github.adrian.wieczorek.local_trade.testutils.UserUtils;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(MockitoExtension.class)
@TestPropertySource(
    properties = "security.jwt.secret-key=41c6701ad7f5abf1db2b053a2f1a39ad41189e00462ec987622b5409dbc0006d")
@Testcontainers
@AutoConfigureMockMvc
public class AdvertisementFilterIntegrationTests extends AbstractIntegrationTest {
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private UsersRepository usersRepository;
  @Autowired
  private AdvertisementRepository advertisementRepository;
  @Autowired
  private CategoryRepository categoryRepository;
  @Autowired
  private MockMvc mockMvc;

  @Test
  @Transactional
  public void filterByCategoryIdAndPageAdvertisements_thenReturnPageOfAdvertisements()
      throws Exception {
    UsersEntity user = UserUtils.createUserRoleUser();
    usersRepository.save(user);
    CategoryEntity categoryEntity = CategoryUtils.createCategoryForIntegrationTests();
    categoryRepository.save(categoryEntity);

    List<AdvertisementEntity> advertisementEntities =
        advertisementRepository.saveAll(IntStream.range(0, 10)
            .mapToObj(
                i -> AdUtils.createAdvertisementRoleUserForIntegrationTests(categoryEntity, user))
            .toList());
    Integer categoryId = categoryEntity.getId();

    mockMvc
        .perform(get("/advertisements/search").with(csrf())
            .param("categoryEntityId", String.valueOf(categoryId)).param("page", "0")
            .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(advertisementEntities.size()))
        .andExpect(jsonPath("$.number").value(0)).andExpect(jsonPath("$.size").value(10));
  }

  @Test
  @Transactional
  public void filterByTitleAndPageAdvertisements_thenReturnPageOfAdvertisements() throws Exception {
    UsersEntity user = UserUtils.createUserRoleUser();
    usersRepository.save(user);
    CategoryEntity categoryEntity = CategoryUtils.createCategoryForIntegrationTests();
    categoryRepository.save(categoryEntity);

    List<AdvertisementEntity> advertisementEntities =
        advertisementRepository.saveAll(IntStream.range(0, 10)
            .mapToObj(
                i -> AdUtils.createAdvertisementRoleUserForIntegrationTests(categoryEntity, user))
            .toList());
    String title = advertisementEntities.get(0).getTitle();

    mockMvc
        .perform(get("/advertisements/search").with(csrf()).param("title", title).param("page", "0")
            .param("size", "10"))

        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(advertisementEntities.size()))
        .andExpect(jsonPath("$.number").value(0)).andExpect(jsonPath("$.size").value(10));
  }

  @Test
  @Transactional
  public void filterAndPageAdvertisements_thenReturnPageOfAdvertisements() throws Exception {
    UsersEntity user = UserUtils.createUserRoleUser();
    usersRepository.save(user);
    CategoryEntity categoryEntity = CategoryUtils.createCategoryForIntegrationTests();
    categoryRepository.save(categoryEntity);

    List<AdvertisementEntity> advertisementEntities =
        advertisementRepository.saveAll(IntStream.range(0, 10)
            .mapToObj(
                i -> AdUtils.createAdvertisementRoleUserForIntegrationTests(categoryEntity, user))
            .toList());

    mockMvc
        .perform(get("/advertisements/search").with(csrf()).param("page", "0").param("size", "10"))

        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(advertisementEntities.size()))
        .andExpect(jsonPath("$.number").value(0)).andExpect(jsonPath("$.size").value(10));
  }

  @Test
  @Transactional
  public void filterByCategoryIdAndFilterForTitlePageAdvertisements_thenReturnPage()
      throws Exception {
    UsersEntity user = UserUtils.createUserRoleUser();
    usersRepository.save(user);
    CategoryEntity categoryEntity = CategoryUtils.createCategoryForIntegrationTests();
    categoryRepository.save(categoryEntity);

    List<AdvertisementEntity> advertisementEntities =
        advertisementRepository.saveAll(IntStream.range(0, 10)
            .mapToObj(
                i -> AdUtils.createAdvertisementRoleUserForIntegrationTests(categoryEntity, user))
            .toList());

    mockMvc
        .perform(get("/advertisements/search").with(csrf())
            .param("categoryEntityId",
                String.valueOf(advertisementEntities.get(0).getCategoryEntity().getId()))
            .param("title", String.valueOf(advertisementEntities.get(0).getTitle()))
            .param("page", "0").param("size", "10"))

        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(advertisementEntities.size()))
        .andExpect(jsonPath("$.number").value(0)).andExpect(jsonPath("$.size").value(10));
  }

  @Test
  @Transactional
  public void filterByCategoryIdAndFilterForTitlePageAdvertisements_when_PriceIsBeyondAdverts_thenReturnPageWithNoMatches()
      throws Exception {
    UsersEntity user = UserUtils.createUserRoleUser();
    usersRepository.save(user);
    CategoryEntity categoryEntity = CategoryUtils.createCategoryForIntegrationTests();
    categoryRepository.save(categoryEntity);

    List<AdvertisementEntity> advertisementEntities =
        advertisementRepository.saveAll(IntStream.range(0, 10)
            .mapToObj(
                i -> AdUtils.createAdvertisementRoleUserForIntegrationTests(categoryEntity, user))
            .toList());

    mockMvc
        .perform(get("/advertisements/search").with(csrf())
            .param("categoryEntityId",
                String.valueOf(advertisementEntities.get(0).getCategoryEntity().getId()))
            .param("title", String.valueOf(advertisementEntities.get(0).getTitle()))
            .param("minPrice", "9999999").param("page", "0").param("size", "10"))

        .andExpect(status().isOk()).andExpect(jsonPath("$.content.length()").value(0))
        .andExpect(jsonPath("$.number").value(0)).andExpect(jsonPath("$.size").value(10));
  }

  @Test
  @Transactional
  public void filterByCategoryIdAndTitleSortDirectionASCAndSortByPrice_thenReturnPage()
      throws Exception {
    UsersEntity user = UserUtils.createUserRoleUser();
    usersRepository.save(user);
    CategoryEntity categoryEntity = CategoryUtils.createCategoryForIntegrationTests();
    categoryRepository.save(categoryEntity);

    List<AdvertisementEntity> advertisementEntities =
        advertisementRepository.saveAll(IntStream.range(0, 10)
            .mapToObj(i -> AdFiltersUtils.createAdvertisementWithIndex(categoryEntity, user, i))
            .toList());

    MvcResult result =
        mockMvc
            .perform(get("/advertisements/search").with(csrf()).param("page", "0")
                .param("size", "10").param("sort", "price,asc"))
            .andExpect(status().isOk()).andReturn();

    String json = result.getResponse().getContentAsString();

    JsonNode root = objectMapper.readTree(json);

    List<BigDecimal> prices = new ArrayList<>();
    root.get("content").forEach(node -> prices.add(node.get("price").decimalValue()));

    List<BigDecimal> sorted = new ArrayList<>(prices);
    sorted.sort(Comparator.naturalOrder());

    assertThat(prices).isEqualTo(sorted);
  }

  @Test
  @Transactional
  public void filterByCategoryIdAndTitleSortDirectionDESCAndSortByTitle_thenReturnPage()
      throws Exception {
    UsersEntity user = UserUtils.createUserRoleUser();
    usersRepository.save(user);
    CategoryEntity categoryEntity = CategoryUtils.createCategoryForIntegrationTests();
    categoryRepository.save(categoryEntity);

    List<AdvertisementEntity> advertisementEntities =
        advertisementRepository.saveAll(IntStream.range(0, 10)
            .mapToObj(i -> AdFiltersUtils.createAdvertisementWithIndex(categoryEntity, user, i))
            .toList());

    MvcResult result =
        mockMvc
            .perform(get("/advertisements/search").with(csrf()).param("page", "0")
                .param("size", "10").param("sort", "title,desc"))
            .andExpect(status().isOk()).andReturn();

    String json = result.getResponse().getContentAsString();

    JsonNode root = objectMapper.readTree(json);

    List<String> titles = new ArrayList<>();
    root.get("content").forEach(node -> titles.add(node.get("title").asText()));

    List<String> sorted = new ArrayList<>(titles);
    sorted.sort(Comparator.reverseOrder());

    assertThat(titles).isEqualTo(sorted);
  }

  @Test
  @Transactional
  public void filterByAllCriteriaAtOnce_shouldReturnOnlyMatchingAdvertisement() throws Exception {
    UsersEntity user = UserUtils.createUserRoleUser();
    usersRepository.save(user);
    CategoryEntity category = CategoryUtils.createCategoryForIntegrationTests();
    categoryRepository.save(category);

    AdvertisementEntity targetAd = new AdvertisementEntity();
    targetAd.setUser(user);
    targetAd.setCategoryEntity(category);
    targetAd.setTitle("Bike");
    targetAd.setDescription("Title");
    targetAd.setPrice(BigDecimal.valueOf(100.00));
    targetAd.setLocation("Warsaw");
    targetAd.setActive(true);
    advertisementRepository.save(targetAd);

    AdvertisementEntity wrongAd = new AdvertisementEntity();
    wrongAd.setUser(user);
    wrongAd.setCategoryEntity(category);
    wrongAd.setTitle("Other Bike");
    wrongAd.setDescription("Other Title");
    wrongAd.setPrice(BigDecimal.valueOf(50.00));
    wrongAd.setLocation("Poznan");
    wrongAd.setActive(true);
    advertisementRepository.save(wrongAd);

    // When & Then
    mockMvc
        .perform(get("/advertisements/search").with(csrf())
            .param("categoryEntityId", String.valueOf(category.getId())).param("title", "Bike") // "
            .param("minPrice", "90").param("maxPrice", "110").param("location", "Warsaw")
            .param("active", "true").param("page", "0").param("size", "10"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].title").value("Bike"));
  }
}
