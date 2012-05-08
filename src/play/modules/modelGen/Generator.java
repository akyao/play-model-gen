package play.modules.modelGen;

import java.io.File;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import me.stormcat.maven.plugin.s2jdbcgen.DelFlag;
import me.stormcat.maven.plugin.s2jdbcgen.GenerateCodeExecutor;
import me.stormcat.maven.plugin.s2jdbcgen.ModelMeta;
import me.stormcat.maven.plugin.s2jdbcgen.factory.ColumnListBuilder;
import me.stormcat.maven.plugin.s2jdbcgen.meta.Column;
import me.stormcat.maven.plugin.s2jdbcgen.meta.Index;
import me.stormcat.maven.plugin.s2jdbcgen.meta.Table;
import me.stormcat.maven.plugin.s2jdbcgen.util.ConnectionUtil;
import me.stormcat.maven.plugin.s2jdbcgen.util.DriverManagerUtil;
import me.stormcat.maven.plugin.s2jdbcgen.util.FileUtil;
import me.stormcat.maven.plugin.s2jdbcgen.util.StringUtil;
//import me.stormcat.maven.plugin.s2jdbcgen.util.VelocityUtil;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.seasar.util.io.ResourceUtil;
import org.seasar.util.sql.PreparedStatementUtil;
import org.seasar.util.sql.ResultSetUtil;
import org.seasar.util.sql.StatementUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

/**
 *
 * only mysql
 *
 * @author konuma_akio
 *
 */
public class Generator {

    private static final String SHOW_TABLES = "SHOW TABLES";

    private final String genDir;

    private final String packageAbstract;

    private final String packageConcrete;

    private final String host;

    private final String schema;

    private final String user;

    private final String password;

    private final String vmAbstract;

    private final String vmConcrete;

    private DelFlag delFlag;

    private static final Logger logger = LoggerFactory.getLogger(GenerateCodeExecutor.class);

    public Generator(String genDir, String packageAbstract, String packageConcrete, String host, String schema, String user, String password, String vmAbstract, String vmConcrete) {
        this.genDir = genDir;
        this.packageAbstract = packageAbstract;
        this.packageConcrete = packageConcrete;
        this.host = host;
        this.schema = schema;
        this.user = user;
        this.password = password;
        this.vmAbstract = vmAbstract;
        this.vmConcrete = vmConcrete;
        init();
    }

    public void execute() {

        String tableNameColumn = String.format("Tables_in_%s", schema);

        DriverManagerUtil.registerDriver("com.mysql.jdbc.Driver");

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet resultSet = null;

        logger.info("-----[configuration]-----");
        logger.info(String.format("genDir=%s", genDir));
        logger.info(String.format("packageAbstract=%s", packageAbstract));
        logger.info(String.format("packageConcrete=%s", packageConcrete));
        logger.info(String.format("host=%s", host));
        logger.info(String.format("schema=%s", schema));
        logger.info(String.format("user=%s", user));
        logger.info(String.format("password=%s", password));
        if (delFlag != null) {
            logger.info(String.format("delFlag#name=%s", delFlag.getName()));
            logger.info(String.format("delFlag#delValue=%s", delFlag.isDelValue()));
        }
        logger.info("-------------------------");

        logger.info(String.format("%s", schema));
        Map<String, ModelMeta> metaMap = new LinkedHashMap<String, ModelMeta>();
        try {
            connection = DriverManagerUtil.getConnection(String.format("jdbc:mysql://%s/%s", host, schema), user, password);
            ps = ConnectionUtil.getPreparedStatement(connection, SHOW_TABLES);
            DatabaseMetaData metaData = connection.getMetaData();

            resultSet = PreparedStatementUtil.executeQuery(ps);
            while (ResultSetUtil.next(resultSet)) {
                String tableName = resultSet.getString(tableNameColumn);
                logger.info(String.format("%s", tableName));
                Table table = metaProcess(metaData, schema, tableName);
                metaMap.put(tableName, new ModelMeta(table));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            ResultSetUtil.close(resultSet);
            StatementUtil.close(ps);
        }

        for (Entry<String, ModelMeta> entry : metaMap.entrySet()) {
            Table table = entry.getValue().getTable();
            for (Column column : table.getColumnList()) {
                String referencedTable = column.getReferenceTableName();
                if (StringUtil.isNotBlank(referencedTable) && metaMap.containsKey(referencedTable)) {
                    logger.info(String.format("relation %s.%s > %s", table.getName(), column.getColumnName(), referencedTable));
                    column.setReferencedModel(metaMap.get(referencedTable.trim()));
                }
            }
        }

        // generate code
        String pathAbstract = String.format("%s/%s", genDir, packageAbstract.replace(".", "/"));
        String pathConcrete = String.format("%s/%s", genDir, packageConcrete.replace(".", "/"));
        FileUtil.mkdirsIfNotExists(pathAbstract);
        FileUtil.mkdirsIfNotExists(pathConcrete);

//        String entityPackage = String.format("%s.entity", rootPackage);

        List<ModelMeta> modelMetaItems = new ArrayList<ModelMeta>();

        for (Entry<String, ModelMeta> entry : metaMap.entrySet()) {
            ModelMeta modelMeta = entry.getValue();

            String abstractEntityFilePath = String.format("%s/%s.java", pathAbstract, modelMeta.getAbstractEntityName());
            String entityFilePath = String.format("%s/%s.java", pathConcrete, modelMeta.getEntityName());

            File abstractEntityFile = new File(abstractEntityFilePath);
            File entityFile = new File(entityFilePath);

            Map<String, Object> params = new HashMap<String, Object>();
            //params.put("entityPackage", entityPackage);
            params.put("meta", modelMeta);
            params.put("delFlag", delFlag);
            params.put("packageAbstract", packageAbstract);
            params.put("packageConcrete", packageConcrete);

            //TODO VMをほげほげ
            logger.info(String.format("generate *%s", modelMeta.getTable().getName()));
            writeContentsToFile(abstractEntityFile, this.vmAbstract, params, true);
            writeContentsToFile(entityFile, this.vmConcrete, params, false);

            modelMetaItems.add(modelMeta);
        }

    }

    private Table metaProcess(DatabaseMetaData metaData, String schema, String tableName) throws Exception {
        try {
            Set<String> primaryKeySet = getPrimaryKeySet(metaData, schema, tableName);
            //ColumnListBuilder columnListBuilder = new ColumnListBuilder(metaData.getColumns(null, schema, tableName, "%"), primaryKeySet);
            ColumnListBuilder columnListBuilder = new ColumnListBuilderForPlay(metaData.getColumns(null, schema, tableName, "%"), primaryKeySet);
            List<Column> columnList = columnListBuilder.build();

            ResultSet rs = metaData.getIndexInfo("", schema, tableName, false, false);

            Map<String, Index> indexMap = new LinkedHashMap<String, Index>();
            while (rs.next()) {
                String indexName = rs.getString("INDEX_NAME");
                if (!"PRIMARY".equals(indexName)) {
                    boolean unique = !rs.getBoolean("NON_UNIQUE");
                    Index index = indexMap.containsKey(indexName) ? indexMap.get(indexName) : new Index(indexName, unique);
                    final String columnName = rs.getString("COLUMN_NAME");
                    Collection<Column> target = Collections2.filter(columnList, new Predicate<Column>() {
                        @Override
                        public boolean apply(Column input) {
                            return input.getColumnName().equals(columnName);
                        }
                    });
                    if (!target.isEmpty()) {
                        index.addColumn(target.iterator().next(), rs.getInt("ORDINAL_POSITION"), rs.getString("ASC_OR_DESC").equals("A"));
                    }

                    indexMap.put(indexName, index);
                }
            }
            rs.close();

            return new Table(tableName, "", columnList, indexMap);
        } catch (Exception e) {
            throw e;
        }
    }

    private Set<String> getPrimaryKeySet(DatabaseMetaData metaData, String schema, String tableName) throws Exception {
        Set<String> columnSet = new LinkedHashSet<String>();
        ResultSet rs = metaData.getPrimaryKeys(null, schema, tableName);
        while (rs.next()) {
            columnSet.add(rs.getString("COLUMN_NAME"));
        }
        rs.close();
        return columnSet;
    }

    private void writeContentsToFile(File file, String template, Map<String, Object> params, boolean overwrite) {
        if (overwrite || !file.exists()) {
            String entityContents = merge(template, params);
            FileUtil.writeStringToFile(file, entityContents, "UTF-8");
            logger.info(String.format("generated |-%s", file.getName()));
        } else {
            logger.info(String.format("skip |-%s", file.getName()));
        }
    }

    public void setDelFlag(DelFlag delFlag) {
        this.delFlag = delFlag;
    }

    public String merge(String templateName, Map<String, Object> params){

        VelocityContext context = new VelocityContext();
        for (Entry<String, Object> entry : params.entrySet()) {
            context.put(entry.getKey(), entry.getValue());
        }
        Template template = Velocity.getTemplate(templateName);
        StringWriter sw = new StringWriter();
        template.merge(context, sw);
        return sw.toString();
    }

    public void init(){
        Properties p = new Properties();
        p.setProperty("file.resource.loader.path", "./, " + this.genDir);
        Velocity.init(p);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        Generator executor
            = new Generator(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8]);
        DelFlag delFlag = new DelFlag();
        delFlag.setName("valid");
        delFlag.setDelValue(false);
        executor.setDelFlag(delFlag);
        executor.execute();
    }
}