package play.modules.modelGen;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.seasar.util.sql.ResultSetUtil;

import com.google.common.collect.ImmutableList;

import me.stormcat.maven.plugin.s2jdbcgen.factory.ColumnListBuilder;
import me.stormcat.maven.plugin.s2jdbcgen.meta.Column;

/**
 *
 * @author konuma_akio
 *
 */
public class ColumnListBuilderForPlay extends ColumnListBuilder{

    protected List<Column> columnList;

    protected final ResultSet resultSet;

    private final Set<String> primaryKeySet;

    public ColumnListBuilderForPlay(ResultSet resultSet, Set<String> primaryKeySet) {
        super(resultSet, primaryKeySet);
        this.columnList = new ArrayList<Column>();
        this.resultSet = resultSet;
        this.primaryKeySet = primaryKeySet;
    }

    /**
     * ${inheritDoc}
     */
    @Override
    public List<Column> build() {
        while (ResultSetUtil.next(resultSet)) {
            Column column = new ColumnForPlay(resultSet, primaryKeySet);
            if(!column.getColumnName().equals("id")){
                // id列は対象外とするので
                columnList.add(column);
            }
        }

        return ImmutableList.copyOf(columnList);
    }
}
