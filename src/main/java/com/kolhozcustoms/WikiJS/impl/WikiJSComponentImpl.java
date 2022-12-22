package com.kolhozcustoms.WikiJS.impl;

import com.atlassian.sal.api.ApplicationProperties;
import com.kolhozcustoms.WikiJS.api.WikiJSComponent;


public class WikiJSComponentImpl implements WikiJSComponent
{
        private final ApplicationProperties applicationProperties;

        public WikiJSComponentImpl(final ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
    }

    public String getName()
    {
        if(null != applicationProperties)
        {
            return "myComponent:" + applicationProperties.getDisplayName();
        }
        
        return "myComponent";
    }
}