package com.nosaiii.sjorm;

import static org.junit.Assert.*;

import com.nosaiii.sjorm.metadata.AbstractModelMetadata;
import com.nosaiii.sjorm.metadata.ModelMetadata;
import com.nosaiii.sjorm.models.DummyModelPrimary;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ SJORM.class })
public class DataAccessTests {
    private SJORM sjorm;
    private SJORMConnection sjormConnection;

    @Before
    public void setup() {
        sjormConnection = Mockito.mock(SJORMConnection.class);
        Mockito.when(sjormConnection.getPrimaryKeys(Mockito.anyString())).thenReturn(new String[] { "id" });

        sjorm = Mockito.mock(SJORM.class);
        Mockito.when(sjorm.getSJORMConnection()).thenReturn(sjormConnection);

        PowerMockito.mockStatic(SJORM.class);
        PowerMockito.when(SJORM.getInstance()).thenReturn(sjorm);

        HashMap<Class<? extends Model>, AbstractModelMetadata> metadatas = new HashMap<>();
        metadatas.put(DummyModelPrimary.class, new ModelMetadata(DummyModelPrimary.class));
        Mockito.when(sjorm.getMetadatas()).thenReturn(metadatas);
    }

    @Test
    public void getAll_WithValidEntries_ShouldReturnCorrectAmount() {
        // Arrange
        DummyModelPrimary[] models = new DummyModelPrimary[] {
                new DummyModelPrimary(),
                new DummyModelPrimary(),
                new DummyModelPrimary(),
                new DummyModelPrimary(),
                new DummyModelPrimary()
        };

        Query<DummyModelPrimary> mockQuery = new Query<>(Arrays.stream(models).collect(Collectors.toList()));
        Mockito.when(sjorm.getAll(DummyModelPrimary.class)).thenReturn(mockQuery);

        // Act
        Query<DummyModelPrimary> query = sjorm.getAll(DummyModelPrimary.class);

        // Assert
        assertEquals(models.length, query.count());
    }

    @Test
    public void getAll_WithValidEntries_ShouldReturnCorrectPropertyValues() {
        // Arrange
        DummyModelPrimary[] models = new DummyModelPrimary[] {
                new DummyModelPrimary() {{
                    setProperty("field1", "cheese");
                }},
                new DummyModelPrimary() {{
                    setProperty("field1", "apple");
                }},
                new DummyModelPrimary() {{
                    setProperty("field1", "bread");
                }}
        };

        Query<DummyModelPrimary> mockQuery = new Query<>(Arrays.stream(models).collect(Collectors.toList()));
        Mockito.when(sjorm.getAll(DummyModelPrimary.class)).thenReturn(mockQuery);

        // Act
        Query<DummyModelPrimary> query = sjorm.getAll(DummyModelPrimary.class);

        // Assert
        assertEquals("cheese", query.first().getProperty("field1", String.class));
        assertEquals("bread", query.last().getProperty("field1", String.class));
    }
}