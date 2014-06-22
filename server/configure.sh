if [ "$JAVA_HOME"  = "" ] ; then
        echo "You  must set  JAVA_HOME env_var first."
        exit
fi

${JAVA_HOME}/bin/java  -cp ./lib/monitor.jar:./lib/ncdepend.jar nc.monitor.ui.serviceconfig.ServiceConfigFrame