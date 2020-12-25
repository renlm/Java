package cn.renlm.plugins;

import static com.baomidou.mybatisplus.core.toolkit.StringPool.DOT_XML;
import static com.baomidou.mybatisplus.core.toolkit.StringPool.SLASH;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.FileOutConfig;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.IFileCreate;
import com.baomidou.mybatisplus.generator.config.PackageConfig;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.TemplateConfig;
import com.baomidou.mybatisplus.generator.config.builder.ConfigBuilder;
import com.baomidou.mybatisplus.generator.config.converts.PostgreSqlTypeConvert;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import com.baomidou.mybatisplus.generator.config.rules.FileType;
import com.baomidou.mybatisplus.generator.config.rules.IColumnType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import cn.renlm.crawler.common.utils.XStreamUtil;
import lombok.Data;

/**
 * 代码生成封装类
 * 
 * @author 任黎明
 *
 */
public class MyCodeGeneratorUtil {
	static final String projectPath 			= System.getProperty("user.dir");
	static final String javaOutputDir 			= projectPath + "/src/main/java";
	static final String mapperSuffix 			= "Mapper";
	static final String mapperOutputDir 		= projectPath + "/src/main/resources/mapper";
	static final String mapperTemplatePath		= "/templates/mapper.xml.ftl";
	static final String serviceImplTemplatePath	= "templates/serviceImpl.java";

	/**
	 * 读取配置并运行
	 * 
	 * @param xml
	 */
	public static final void run(String xml) {
		GeneratorConfig conf = XStreamUtil.read(GeneratorConfig.class, xml);
		DataSourceConfig dsc = new DataSourceConfig()
				.setUrl(conf.url)
				.setUsername(conf.username)
				.setPassword(conf.password)
				.setDriverName(conf.driverName);
		DbType dbType = dsc.getDbType();
		if (dbType == DbType.POSTGRE_SQL) {
			dsc.setTypeConvert(new PostgreSqlTypeConvert() {
				@Override
				public IColumnType processTypeConvert(GlobalConfig globalConfig, String fieldType) {
					if (fieldType.toLowerCase().contains("bytea")) {
						return DbColumnType.BYTE_ARRAY;
					}
					return super.processTypeConvert(globalConfig, fieldType);
				}

			});
		}
		conf.modules.forEach(module -> {
			module.tables.forEach(table -> {
				create(conf, dsc, module.pkg, module.name, table);
			});
		});
	}

	private static final void create(GeneratorConfig conf, DataSourceConfig dsc, String pkg, String moduleName,
			GeneratorTable table) {
		AutoGenerator mpg = new AutoGenerator();
		mpg.setTemplateEngine(new FreemarkerTemplateEngine());

		// 全局配置
		GlobalConfig gc = new GlobalConfig();
		gc.setOutputDir(javaOutputDir);
		gc.setAuthor(table.author);
		gc.setOpen(false);
		gc.setIdType(table.idType == null ? IdType.AUTO : IdType.valueOf(table.idType));
		gc.setDateType(DateType.ONLY_DATE);
		mpg.setGlobalConfig(gc);

		// 数据源配置
		mpg.setDataSource(dsc);

		// 包配置
		PackageConfig pc = new PackageConfig();
		pc.setModuleName(moduleName);
		pc.setParent(pkg);
		mpg.setPackageInfo(pc);

		// 自定义配置
		InjectionConfig cfg = new InjectionConfig() {
			@Override
			public void initMap() {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("dsName", conf.dsName);
				map.put("nameOfDS", DS.class.getName());
				this.setMap(map);
			}
		};

		// 自定义输出配置
		List<FileOutConfig> focList = new ArrayList<>();
		// 自定义配置会被优先输出
		focList.add(new FileOutConfig(mapperTemplatePath) {
			@Override
			public String outputFile(TableInfo tableInfo) {
				// 自定义输出文件名 ， 如果你 Entity 设置了前后缀、此处注意 xml 的名称会跟着发生变化！！
				return mapperOutputDir + SLASH 
						+ pc.getModuleName() + SLASH 
						+ tableInfo.getEntityName() + mapperSuffix + DOT_XML;
			}
		});
		cfg.setFileCreate(new IFileCreate() {
			@Override
			public boolean isCreate(ConfigBuilder configBuilder, FileType fileType, String filePath) {
				this.checkDir(filePath);
				return fileType == FileType.ENTITY || !new File(filePath).exists();
			}

			@Override
			public void checkDir(String filePath) {
				File file = new File(filePath);
				boolean exist = file.exists();
				if (!exist) {
					file.getParentFile().mkdirs();
				}
			}
		});
		cfg.setFileOutConfigList(focList);
		mpg.setCfg(cfg);

		// 配置模板
		TemplateConfig templateConfig = new TemplateConfig();

		templateConfig.setXml(null);
		templateConfig.setServiceImpl(serviceImplTemplatePath);
		templateConfig.setController(null);
		mpg.setTemplate(templateConfig);

		// 策略配置
		StrategyConfig strategy = new StrategyConfig();
		strategy.setNaming(NamingStrategy.underline_to_camel);
		strategy.setColumnNaming(NamingStrategy.underline_to_camel);
		strategy.setEntityLombokModel(true);
		strategy.setEntityTableFieldAnnotationEnable(true);
		strategy.setInclude(table.name);
		mpg.setStrategy(strategy);
		mpg.execute();
	}

	@Data
	@XStreamAlias("generator")
	public static final class GeneratorConfig implements Serializable {
		private static final long serialVersionUID = 1L;

		@XStreamAsAttribute
		private String dsName;

		private String url;

		private String username;

		private String password;

		private String driverName;

		@XStreamImplicit(itemFieldName = "module")
		private List<GeneratorModule> modules;

	}

	@Data
	public static final class GeneratorModule implements Serializable {
		private static final long serialVersionUID = 1L;

		@XStreamAsAttribute
		private String name;

		@XStreamAsAttribute
		@XStreamAlias("package")
		private String pkg;

		@XStreamImplicit(itemFieldName = "table")
		private List<GeneratorTable> tables;

	}

	@Data
	public static final class GeneratorTable implements Serializable {
		private static final long serialVersionUID = 1L;

		@XStreamAsAttribute
		private String schema;

		@XStreamAsAttribute
		private String author;

		@XStreamAsAttribute
		private String name;

		@XStreamAsAttribute
		private String idType;

	}
}