package com.nosaiii.sjorm;

import static org.junit.Assert.*;

import com.nosaiii.sjorm.metadata.AbstractModelMetadata;
import com.nosaiii.sjorm.metadata.ModelMetadata;
import com.nosaiii.sjorm.models.DummyModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

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
        AbstractModelMetadata modelMetadata = new ModelMetadata(DummyModel.class);

        // Act
        sjorm.registerModel(modelMetadata);

        // Assert
        assertTrue(sjorm.getMetadatas().containsKey(DummyModel.class));
        assertNotNull(sjorm.getMetadata(DummyModel.class));
    }

    @Test
    public void getMetadata_WithRegisteredModel_ShouldReturnValidModelMetadata() {
        // Arrange
        AbstractModelMetadata registeredModelMetadata = new ModelMetadata(DummyModel.class);

        // Act
        sjorm.registerModel(registeredModelMetadata);
        AbstractModelMetadata retrievedModelMetadata = sjorm.getMetadata(DummyModel.class);

        // Assert
        assertNotNull(retrievedModelMetadata);
        assertEquals(retrievedModelMetadata.getType(), DummyModel.class);
        assertEquals("dummy", retrievedModelMetadata.getTable());
        assertArrayEquals(new String[] { "id" }, retrievedModelMetadata.getPrimaryKeyFields());
    }
}