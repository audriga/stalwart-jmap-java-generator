package com.audriga.stalwartgenerator.model;

public sealed interface GenSchemaType extends GenClass permits GenStruct, GenSealed {}
