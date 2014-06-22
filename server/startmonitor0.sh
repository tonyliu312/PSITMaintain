if [ "$JAVA_HOME" = "" ] : then
        echo "You must set JAVA_HOME env_var first."
        exit
fi
export LANG=zh_CN.gb18030
export CLASSPATH=./lib/monitor.jar:./lib/wasdepend.jar:./lib/ncdepend.jar:./lib/wassslprotocol.jar:./lib/activation-1.1.1.jar:./lib/annotations-api.jar:./lib/catalina-ant.jar:./lib/catalina-ha.jar:./lib/catalina-tribes.jar:./lib/catalina.jar:./lib/commons-collections.jar:./lib/db2java.jar:./lib/db2jcc.jar:./lib/db2jcc_license_cisuz.jar:./lib/db2jcc_license_cu.jarel-api.jar:./lib/jasper-el.jar:./lib/jasper-jdt.jar:./lib/jasper.jar:./lib/jsp-api.jar:./lib/mx.jar:./lib/ojdbc14.jar:./lib/servlet-api.jar:./lib/sqljdbc.jar:./lib/tomcat-coyote.jar:./lib/tomcat-dbcp.jar:./lib/tomcat-i18n-es.jar:./lib/tomcat-i18n-fr.jar:./lib/tomcat-i18n-ja.jar:./lib/xml.jar:bin/tomcat-juli.jar:./lib/ChartDirector.jar:./lib/zip.jar;
${JAVA_HOME}/bin/java  -Xmx200m  -Dcatalina.home=. -cp $CLASSPATH org.apache.catalina.startup.Catalina start
