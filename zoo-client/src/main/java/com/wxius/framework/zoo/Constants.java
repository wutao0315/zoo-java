package com.wxius.framework.zoo;

public class Constants {
    public static final String ClientVersion = "Zoo-Client-Java:v1.0.0";
    public static final String COLON = ":";
    public static final String HTTP_PREFIX = "http";
    //系统环境变量-应用名称
    public static final String EnvironmentApplicationName = "ASPNETCORE_APPLICATIONNAME";
    // 环境变量启动实例
    public static final String EnvironmentApplicationId = "ASPNETCORE_APPLICATIONID";
    public static final String DefaultApplication = "appsettings";
    public static final String DefaultGroupName = "default_group";
    public static final String DefaultNamespaceId = "public";
    public static final String DefaultServiceName = "default";
    public static final String Separator = "+";
    public static final String Suffix = "json";
    public static final String FileSuffix = "."+Suffix;
    public static final String ConfigFileContentKey = "content";
    public static final String DefaultServerUrl = "http://localhost:6401";
    public static final String DefaultTypeName = "cfg";
    public static final String DefaultGroupSplit = "@@";
    public static final String DefaultNamespaceSplit = "$$";
    public static final String AuthenticationScheme = "Zoo";
    public static final String ConfigService = "ZooService";
    public static final String Encrypt = "_encrypt_";
    // label key value  define.
    public static final String LABEL_SOURCE = "source";
    public static final String LABEL_SOURCE_SDK = "sdk";
    public static final String LABEL_SOURCE_CLUSTER = "cluster";
    public static final String LABEL_MODULE = "module";
    public static final String LABEL_MODULE_CONFIG = "config";
    public static final String LABEL_MODULE_NAMING = "naming";
    public static final boolean IsUnix  = !java.lang.System.getProperty("os.name").contains("Windows");
    public static final String DefaultLocalCacheDir = IsUnix ? "/opt/data" : "C:\\opt\\data";
}
