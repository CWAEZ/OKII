/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */
package org.elasticsearch.xpack.sql.expression.function.scalar.string;

import org.elasticsearch.xpack.ql.expression.Expression;
import org.elasticsearch.xpack.ql.expression.gen.pipeline.BinaryPipe;
import org.elasticsearch.xpack.ql.expression.gen.pipeline.Pipe;
import org.elasticsearch.xpack.ql.tree.NodeInfo;
import org.elasticsearch.xpack.ql.tree.Source;
import org.elasticsearch.xpack.sql.expression.function.scalar.string.BinaryStringNumericProcessor.BinaryStringNumericOperation;

import java.util.Objects;

/**
 * String operations pipe requiring one string and one numeric argument.
 */
public class BinaryStringNumericPipe extends BinaryPipe {

    private final BinaryStringNumericOperation operation;

    public BinaryStringNumericPipe(Source source, Expression expression, Pipe left, Pipe right, BinaryStringNumericOperation operation) {
        super(source, expression, left, right);
        this.operation = operation;
    }

    @Override
    protected NodeInfo<BinaryStringNumericPipe> info() {
        return NodeInfo.create(this, BinaryStringNumericPipe::new, expression(), left(), right(), operation());
    }

    public BinaryStringNumericOperation operation() {
        return operation;
    }

    @Override
    protected BinaryPipe replaceChildren(Pipe newLeft, Pipe newRight) {
        return new BinaryStringNumericPipe(source(), expression(), newLeft, newRight, operation());
    }

    @Override
    public BinaryStringNumericProcessor asProcessor() {
        return new BinaryStringNumericProcessor(left().asProcessor(), right().asProcessor(), operation());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), operation);
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            BinaryStringNumericPipe other = (BinaryStringNumericPipe) obj;
            return Objects.equals(operation, other.operation);
        }
        return false;
    }
}
