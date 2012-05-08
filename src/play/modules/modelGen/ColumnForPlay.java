package play.modules.modelGen;

import java.sql.ResultSet;
import java.util.Set;

import me.stormcat.maven.plugin.s2jdbcgen.Constants.ColumnMetaColumn;
import me.stormcat.maven.plugin.s2jdbcgen.meta.Column;


public class ColumnForPlay extends Column{

    public ColumnForPlay(ResultSet resultSet, Set<String> primaryKeySet) {
        super(resultSet, primaryKeySet);
    }

    @Override
    public String getColumnAnnotation() {
        StringBuilder builder = new StringBuilder("@Column(");
        //name
        builder.append(String.format("name = \"%s\"", getFieldName()));
        // nullable
        builder.append(", nullable = ");
        builder.append(isNullable());
        // unique
        // insertable
        // updatable
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

        //name

        //nullable

        //length

        builder.append(")");
        return builder.toString();
    }
}
