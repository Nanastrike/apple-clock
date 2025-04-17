package model.config;

import java.sql.Types;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.identity.IdentityColumnSupport;
import org.hibernate.dialect.identity.IdentityColumnSupportImpl;
import org.hibernate.query.sqm.function.SqmFunctionRegistry;
import org.hibernate.query.sqm.produce.function.FunctionContributions;
import org.hibernate.query.sqm.produce.function.FunctionContributor;
import org.hibernate.type.BasicTypeReference;

public class MySQLiteDialect extends Dialect implements FunctionContributor {

    public MySQLiteDialect() {
        super();
        // 注册 SQLite 的字段类型
        registerColumnType(Types.INTEGER, "integer");
        registerColumnType(Types.VARCHAR, "text");
        registerColumnType(Types.BOOLEAN, "boolean");
        registerColumnType(Types.DOUBLE, "real");
        registerColumnType(Types.FLOAT, "real");
    }

    @Override
    public IdentityColumnSupport getIdentityColumnSupport() {
        return new IdentityColumnSupportImpl();
    }

    @Override
    public boolean supportsIfExistsBeforeTableName() {
        return true;
    }

    @Override
    public boolean supportsIfExistsAfterTableName() {
        return true;
    }

    @Override
    public boolean supportsCascadeDelete() {
        return false;
    }

    // 注册 SQLite 支持的一些函数
    @Override
    public void contributeFunctions(FunctionContributions functionContributions, SqmFunctionRegistry registry) {
        var typeConfig = functionContributions.getTypeConfiguration();

        registry.registerPattern(
                "lower", "lower(?1)",
                new BasicTypeReference<>(typeConfig.getBasicTypeRegistry().resolve("string"))
        );
        registry.registerPattern(
                "upper", "upper(?1)",
                new BasicTypeReference<>(typeConfig.getBasicTypeRegistry().resolve("string"))
        );
    }
}
