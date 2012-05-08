package play.modules.modelGen;

import java.sql.ResultSet;
import java.sql.Types;
import java.util.Set;

import me.stormcat.maven.plugin.s2jdbcgen.Constants.ColumnMetaColumn;
import me.stormcat.maven.plugin.s2jdbcgen.Constants.MappedType;
import me.stormcat.maven.plugin.s2jdbcgen.meta.Column;

/**
 *
 * @author konuma_akio
 *
 */
public class ColumnForPlay extends Column{

    public ColumnForPlay(ResultSet resultSet, Set<String> primaryKeySet) {
        super(resultSet, primaryKeySet);
    }

    @Override
    public String getColumnAnnotation() {
        StringBuilder builder = new StringBuilder("@Column(");
        //name
        builder.append(String.format("name = \"%s\"", getColumnName()));
        // nullable
        builder.append(", nullable = ");
        builder.append(isNullable());
        // length
        if (isStringType()) {
            builder.append(", ");
            builder.append("length = ");
            builder.append(getColumnSize());
        } else if (isNumberType()) {
            builder.append(", ");
            builder.append("precision = ");
            builder.append(getColumnSize());
        }
        // unique
        // insertable
        // updatable

        builder.append(")");
        return builder.toString();
    }

    @Override
    public String getJavaType() {
        switch (getDataType()) {
            case Types.DATE:
                return "java.util.Date";
            case Types.TIMESTAMP:
                return "java.util.Date";
            case Types.TIME:
                return "java.util.Date";
            default:
                return super.getJavaType();
        }
    }
}
