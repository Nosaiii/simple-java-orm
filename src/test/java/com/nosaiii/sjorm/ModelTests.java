package com.nosaiii.sjorm;

import static org.junit.Assert.*;

import com.nosaiii.sjorm.exceptions.ModelMetadataNotRegisteredException;
import com.nosaiii.sjorm.metadata.AbstractModelMetadata;
import com.nosaiii.sjorm.metadata.ModelMetadata;
import com.nosaiii.sjorm.models.DummyModelPrimary;
import com.nosaiii.sjorm.models.DummyModelSecondary;
import org.junit.Before;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ SJORM.class })
public class ModelTests {
    private SJORM sjorm;
    private SJORMConnection sjormConnection;

    private HashMap<Class<? extends Model>, AbstractModelMetadata> metadatas;

    @Before
    public void setup() {
        metadatas = new HashMap<>();

        sjormConnection = Mockito.mock(SJORMConnection.class);
        Mockito.when(sjormConnection.getPrimaryKeys(Mockito.anyString())).thenReturn(new String[] { "id" });

        sjorm = Mockito.mock(SJORM.class);
        Mockito.doCallRealMethod().when(sjorm).registerModel(Mockito.any(AbstractModelMetadata.class));
        Mockito.doCallRealMethod().when(sjorm).getMetadata(Mockito.any());
        Mockito.when(sjorm.getMetadatas()).thenReturn(metadatas);
        Mockito.when(sjorm.getSJORMConnection()).thenReturn(sjormConnection);

        PowerMockito.mockStatic(SJORM.class);
        PowerMockito.when(SJORM.getInstance()).thenReturn(sjorm);
    }

    @Test
    public void registerModel_WithValidModel_ShouldRegisterModelMetadata() {
        // Arrange
        AbstractModelMetadata modelMetadata = new ModelMetadata(DummyModelPrimary.class);

        // Act
        sjorm.registerModel(modelMetadata);

        // Assert
        assertTrue(sjorm.getMetadatas().containsKey(DummyModelPrimary.class));
        assertNotNull(sjorm.getMetadata(DummyModelPrimary.class));
    }

    @Test
    public void getMetadata_WithRegisteredModel_ShouldReturnValidModelMetadata() {
        // Arrange
        AbstractModelMetadata registeredModelMetadata = new ModelMetadata(DummyModelPrimary.class);

        // Act
        sjorm.registerModel(registeredModelMetadata);
        AbstractModelMetadata retrievedModelMetadata = sjorm.getMetadata(DummyModelPrimary.class);

        // Assert
        assertNotNull(retrievedModelMetadata);
        assertEquals(retrievedModelMetadata.getType(), DummyModelPrimary.class);
        assertEquals("dummy_primary", retrievedModelMetadata.getTable());
        assertArrayEquals(new String[] { "id" }, retrievedModelMetadata.getPrimaryKeyFields());
    }

    @Test
    public void getMetadata_WithUnregisteredModel_ShouldThrowModelMetadataNotRegisteredException() {
        // Arrange
        // ! Not required

        // Act
        ThrowingRunnable act = () -> sjorm.getMetadata(DummyModelPrimary.class);

        // Assert
        assertThrows(ModelMetadataNotRegisteredException.class, act);
    }

    @Test
    public void newInstance_FromConstructorCall_ShouldReturnNewModel() {
        // Arrange
        AbstractModelMetadata modelMetadata = new ModelMetadata(DummyModelPrimary.class);

        // Act
        sjorm.registerModel(modelMetadata);
        ThrowingRunnable act = DummyModelPrimary::new;

        // Assert
        try {
            act.run();
            assertTrue(true);
        } catch(ModelMetadataNotRegisteredException exception) {
            fail();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @Test
    public void hasMany_FromValidModel_ShouldReturnManyAssociatedModels() {
        // Arrange
        AbstractModelMetadata primaryModelMetadata = new ModelMetadata(DummyModelPrimary.class);
        AbstractModelMetadata secondaryModelMetadata = new ModelMetadata(DummyModelSecondary.class);

        sjorm.registerModel(primaryModelMetadata);
        sjorm.registerModel(secondaryModelMetadata);

        DummyModelPrimary dummyModelPrimary = Mockito.mock(DummyModelPrimary.class);
        Query<DummyModelSecondary> secondariesMock = new Query<>(new ArrayList<>(Arrays.asList(
                new DummyModelSecondary(),
                new DummyModelSecondary(),
                new DummyModelSecondary()
        )));
        Mockito.when(dummyModelPrimary.getSecondaries()).thenReturn(secondariesMock);

        // Act
        sjorm.registerModel(primaryModelMetadata);
        sjorm.registerModel(secondaryModelMetadata);
        Query<DummyModelSecondary> secondaries = dummyModelPrimary.getSecondaries();

        // Assert
        assertNotNull(secondaries);
        assertEquals(3, secondaries.count());
    }
}