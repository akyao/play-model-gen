package play.modules.modelGen;

import java.sql.ResultSet;
import java.sql.Types;
import java.util.Set;

import me.stormcat.maven.plugin.s2jdbcgen.Constants.ColumnMetaColumn;
import me.stormcat.maven.plugin.s2jdbcgen.Constants.MappedType;
import me.stormcat.maven.plugin.s2jdbcgen.meta.CodeDef;
import me.stormcat.maven.plugin.s2jdbcgen.meta.Column;
import me.stormcat.maven.plugin.s2jdbcgen.util.StringUtil;

/**
 *
 * @author konuma_akio
 *
 */
public class ColumnForPlay extends Column{
    
    private CodeDef codeDef;

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

    public String getDefaultValue() {
        StringBuilder builder = new StringBuilder();
        if(StringUtil.isNotEmpty(getColumnDef())){
            String defaultValue = getColumnDef();
            if(defaultValue.equals("b'0'")){
                defaultValue = Boolean.FALSE.toString();
            } else if(defaultValue.equals("b'1'")){
                defaultValue = Boolean.TRUE.toString();
            } else if (defaultValue.equals("0")) {
                if (getJavaType().equals(MappedType.LONG_W)) {
                    defaultValue = "0L";
                } else if (getJavaType().equals(MappedType.INTEGER_W)) {
                    defaultValue = "0";
                } else if (getJavaType().equals(MappedType.FLOAT_W)) {
                    defaultValue = "0.0f";
                } else if (getJavaType().equals(MappedType.DOUBLE_W)) {
                    defaultValue = "0.0";
                }
            }
            builder.append(" = " + defaultValue);
        }
        return builder.toString();
    }

    
    /**
     * codeDef を取得します。
     * @return codeDef
     */
    @Override
    public CodeDef getCodeDef() {
        if (codeDef != null) {
            return codeDef;
        }
        return super.getCodeDef();
    }

    
    /**
     * codeDefを設定します。
     * @param codeDef codeDef
     */
    public void setCodeDef(CodeDef codeDef) {
        this.codeDef = codeDef;
    }
    
    
    
    
}
