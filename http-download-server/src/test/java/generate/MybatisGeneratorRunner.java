package generate;

import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.api.ShellCallback;
import org.mybatis.generator.config.*;
import org.mybatis.generator.exception.InvalidConfigurationException;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MybatisGeneratorRunner {
    public static final int SELECT_BY_PRIMARY = 1;
    public static final int UPDATE_BY_PRIMARY = 2;
    public static final int DELETE_BY_PRIMARY = 4;

    public static void main(String[] args) throws InvalidConfigurationException, SQLException, IOException, InterruptedException {
        Context context = new Context(ModelType.FLAT);
        context.setId("kumiko");
        context.setTargetRuntime("Mybatis3Simple");
        context.addProperty("beginningDelimiter", "`");
        context.addProperty("endingDelimiter", "`");
        addJdbcConnection(context);
        addCommentGenerator(context);
        generateModel(context);
        generateSqlMap(context);
        generateJavaClient(context);

        addTable(context, "settings", "SettingsDO", "SettingsDAO", SELECT_BY_PRIMARY | UPDATE_BY_PRIMARY | DELETE_BY_PRIMARY, null);

        Configuration configuration = new Configuration();
        configuration.addContext(context);
        List<String> warnings = new ArrayList<>();
        // 是否覆盖原有文件（仅java文件，xml文件无法覆盖）
        ShellCallback shellCallback = new DefaultShellCallback(true);
        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(configuration, shellCallback, warnings);

        Set<String> tables = new HashSet<>();

        // 如果不指定tables，默认获取所有table标签；writeFiles为false时不会生成文件，仅供调试使用
        myBatisGenerator.generate(null, null, tables, true);

        // 执行的预警或报错，会通过warning打印出来
        for (String warning : warnings) {
            System.out.println(warning);
        }
    }

    private static TableConfiguration addTable(Context context, String tableName, String domainObjectName, String mapperName, int code,
                                               List<String> columnOverrides) {
        TableConfiguration configuration = new TableConfiguration(context);
        configuration.setTableName(tableName);
        configuration.setDomainObjectName(domainObjectName);
        configuration.setMapperName(mapperName);
        if ((code & SELECT_BY_PRIMARY) == 0) {
            configuration.setSelectByPrimaryKeyStatementEnabled(false);
        }
        if ((code & UPDATE_BY_PRIMARY) == 0) {
            configuration.setUpdateByPrimaryKeyStatementEnabled(false);
        }
        if ((code & DELETE_BY_PRIMARY) == 0) {
            configuration.setDeleteByPrimaryKeyStatementEnabled(false);
        }
        if (columnOverrides != null) {
            for (String s : columnOverrides) {
                ColumnOverride columnOverride = new ColumnOverride(s);
                columnOverride.setGeneratedAlways(true);
                configuration.addColumnOverride(columnOverride);
            }
        }

        context.addTableConfiguration(configuration);
        return configuration;
    }

    private static void addJdbcConnection(Context context) {
        JDBCConnectionConfiguration configuration = new JDBCConnectionConfiguration();
        configuration.setConnectionURL("jdbc:mysql://45.32.92.83:33307/downloadserver");
        configuration.setUserId("reina");
        configuration.setPassword("e^(i*PI)+1=0");
        configuration.setDriverClass("com.mysql.cj.jdbc.Driver");
        context.setJdbcConnectionConfiguration(configuration);
    }

    private static void addCommentGenerator(Context context) {
        CommentGeneratorConfiguration commentGeneratorConfiguration = new CommentGeneratorConfiguration();
        commentGeneratorConfiguration.setConfigurationType("wiki.moe.kumiko.util.RegularCommentGenerator");
        commentGeneratorConfiguration.addProperty("author", "zy");
        commentGeneratorConfiguration.addProperty("suppressDate", "true");
        commentGeneratorConfiguration.addProperty("suppressAllComments", "true");
        context.setCommentGeneratorConfiguration(commentGeneratorConfiguration);
    }

    private static void generateModel(Context context) {
        JavaModelGeneratorConfiguration configuration = new JavaModelGeneratorConfiguration();
        configuration.setTargetPackage("wiki.moe.kumiko.dal.dataobject");
        configuration.setTargetProject("src/main/java");
        configuration.addProperty("enableSubPackages", "true");
        configuration.addProperty("trimStrings", "false");
        context.setJavaModelGeneratorConfiguration(configuration);
    }

    private static void generateSqlMap(Context context) {
        SqlMapGeneratorConfiguration configuration = new SqlMapGeneratorConfiguration();
        configuration.setTargetPackage("mapper");
        configuration.setTargetProject("src/main/resources");
        context.setSqlMapGeneratorConfiguration(configuration);
    }

    private static void generateJavaClient(Context context) {
        JavaClientGeneratorConfiguration configuration = new JavaClientGeneratorConfiguration();
        configuration.setConfigurationType("XMLMAPPER");
        configuration.setTargetPackage("wiki.moe.kumiko.dal.mapper");
        configuration.setTargetProject("src/main/java");
        configuration.addProperty("enableSubPackages", "true");
        context.setJavaClientGeneratorConfiguration(configuration);
    }
}