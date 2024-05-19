package com.avaya.oceanareferenceclient.settings.pojos;

import java.util.Set;

public interface AvayaPlatformPreferences {



    public void setAvailable(boolean available);

    public boolean isAvailable();

    public boolean isSecure();

    public void setSecure(boolean secure);

    public String getAmcServer();

    public void setAmcServer(String amcServer);

    public int getAmcPort();

    public void setAmcPort(int amcPort);

    public String getAmcUrlPath();

    public void setAmcUrlPath(String amcUrlPath) ;

    public String getDestination();

    public void setDestination(String destination);

    public String getContext();

    public void setContext(String context);

    public String getTopic();

    public void setTopic(String topic);

    public String getPriority();

    public void setPriority(String priority);

    public String getLocale();

    public void setLocale(String locale);

    public String getStrategy();

    public void setStrategy(String strategy);

    public String getSourceName();

    public void setSourceName(String sourceName);

    public String getResourceId() ;

    public void setResourceId(String resourceId);


}
