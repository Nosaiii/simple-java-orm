package com.nosaiii.sjorm.metadata;

import com.nosaiii.sjorm.Model;

public class ModelMetadata extends AbstractModelMetadata {
    public ModelMetadata(Class<? extends Model> type) {
        super(type);
    }
}