package io.github.adrian.wieczorek.local_trade.unit;

import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class AdvertisementEntityUnitTests {

  private AdvertisementEntity entity1;
  private AdvertisementEntity entity2;

  @BeforeEach
  void setUp() {
    entity1 = new AdvertisementEntity();
    entity2 = new AdvertisementEntity();
  }

  @Test
  @DisplayName("Test Reflexivity: An object must equal itself")
  void equals_shouldBeReflexive() {
    assertEquals(entity1, entity1, "An object should be equal to itself");
  }

  @Test
  @DisplayName("Test Null Comparison: An object must not equal null")
  void equals_shouldReturnFalse_whenComparedToNull() {
    assertNotEquals(null, entity1, "An object should not be equal to null");
  }

  @Test
  @DisplayName("Test Type Comparison: An object must not equal an object of a different class")
  void equals_shouldReturnFalse_whenComparedToDifferentClass() {
    String otherObject = "This is a String";
    assertNotEquals(entity1, otherObject,
        "An object should not be equal to an object of a different type");
  }

  @Test
  @DisplayName("Test ID Logic: Should return true when advertisementIds are the same")
  void equals_shouldReturnTrue_whenIdsAreSame() {
    UUID sharedUuid = entity1.getAdvertisementId();
    entity2.setAdvertisementId(sharedUuid);

    assertEquals(entity1, entity2, "Objects with the same advertisementId should be equal");
  }

  @Test
  @DisplayName("Test ID Logic: Should return false when advertisementIds are different")
  void equals_shouldReturnFalse_whenIdsAreDifferent() {
    assertNotEquals(entity1, entity2,
        "Objects with different advertisementIds should not be equal");
  }

  @Test
  @DisplayName("Test ID Logic: Should return false when one advertisementId is null")
  void equals_shouldReturnFalse_whenOneIdIsNull() {
    entity1.setAdvertisementId(null);
    assertNotEquals(entity1, entity2,
        "An object with an ID should not be equal to an object with a null ID");
    assertNotEquals(entity2, entity1,
        "An object with a null ID should not be equal to an object with an ID");
  }

  @Test
  @DisplayName("hashCode Contract: Equal objects (non-null ID) must have the same hashCode")
  void hashCode_contract_whenIdsAreSame() {
    UUID sharedUuid = entity1.getAdvertisementId();
    entity2.setAdvertisementId(sharedUuid);
    assertEquals(entity1, entity2, "Precondition failed: Objects must be equal");
    assertEquals(entity1.hashCode(), entity2.hashCode(),
        "Equal objects must have the same hashCode");
  }

}
