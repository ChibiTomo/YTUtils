package net.chibidevteam.utils.helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySources;
import org.springframework.core.env.PropertySourcesPropertyResolver;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class MessageUtils extends PropertySourcesPlaceholderConfigurer {

    private static final String                 RESOURCE_NAME = "message";
    private static final String                 RESOURCE_EXT  = ".properties";

    private static MessageUtils                 msgUtils;

    private static Map<String, PropertySources> msgsMap       = new HashMap<>();

    private ConfigurableListableBeanFactory     beanFactory;
    private Environment                         environment;

    /**
     * {@inheritDoc}
     * <p>
     * {@code PropertySources} from this environment will be searched when replacing ${...} placeholders.
     *
     * @see #setPropertySources
     * @see #postProcessBeanFactory
     */
    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        setFileEncoding("UTF-8");
        setIgnoreResourceNotFound(true);

        this.beanFactory = beanFactory;
        if (msgUtils == null) {
            msgUtils = this;
        }
    }

    private PropertySources getLocalizedMessage(Locale locale) {
        PropertySources result = msgsMap.get(locale.toString());
        if (result != null) {
            return result;
        }
        msgUtils.loadLocaleLocations(locale);

        MutablePropertySources ps = new MutablePropertySources();
        if (this.environment != null) {
            ps.addLast(new PropertySource<Environment>(ENVIRONMENT_PROPERTIES_PROPERTY_SOURCE_NAME, this.environment) {

                @Override
                public String getProperty(String key) {
                    return this.source.getProperty(key);
                }
            });
        }
        try {
            PropertySource<?> localPropertySource = new PropertiesPropertySource(locale.toString(), mergeProperties());
            ps.addFirst(localPropertySource);
        } catch (IOException ex) {
            throw new BeanInitializationException("Could not load properties", ex);
        }

        processProperties(beanFactory, new PropertySourcesPropertyResolver(ps));

        msgsMap.put(locale.toString(), ps);
        return ps;
    }

    private void loadLocaleLocations(Locale locale) {
        String tmp = locale.toString();
        String fname;

        List<Resource> locs = new ArrayList<>();
        while (!StringUtils.isEmpty(tmp)) {
            fname = RESOURCE_NAME + "_" + tmp + RESOURCE_EXT;
            locs.add(new ClassPathResource(fname));
            int last = tmp.lastIndexOf('_');
            if (last >= 0) {
                tmp = tmp.substring(0, last);
            } else {
                tmp = null;
            }
        }

        locs.add(new ClassPathResource(RESOURCE_NAME + RESOURCE_EXT));
        Collections.reverse(locs);

        setLocations(locs.toArray(new Resource[] {}));
    }

    public static String get(String key) {
        return get(key, LocaleContextHolder.getLocale());
    }

    public static String get(String key, Locale locale) {
        PropertySources ps = msgsMap.get(locale.toString());
        if (ps == null) {
            ps = msgUtils.getLocalizedMessage(locale);
        }

        String result = "???" + key + "???";

        Iterator<PropertySource<?>> it = ps.iterator();
        PropertySource<?> p;
        Object obj = null;
        while (it.hasNext() && obj == null) {
            p = it.next();
            obj = p.getProperty(key);
            if (obj != null) {
                result = obj.toString();
            }
        }
        return result;
    }
}
