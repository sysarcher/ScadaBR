/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package br.org.scadabr.web.taglib.dijit;

import br.org.scadabr.utils.i18n.LocalizableEnum;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.servlet.jsp.JspException;

import org.springframework.web.servlet.tags.form.TagWriter;

@SuppressWarnings("serial")
public class SelectTag extends org.springframework.web.servlet.tags.form.SelectTag {

    class LocalizedOption {

        private final String value;
        private final String label;

        public LocalizedOption(String label, String value) {
            this.label = label;
            this.value = value;
        }

        /**
         * @return the value
         */
        public String getValue() {
            return value;
        }

        /**
         * @return the label
         */
        public String getLabel() {
            return label;
        }
    }

    private String i18nLabel;
    private String i18nTitle;

    /**
     * Renders the HTML '{@code select}' tag to the supplied {@link TagWriter}.
     * <p>
     * Renders nested '{@code option}' tags if the {@link #setItems items}
     * property is set, otherwise exposes the bound value for the nested
     * {@link OptionTag OptionTags}.
     */
    @Override
    protected int writeTagContent(TagWriter tagWriter) throws JspException {
        if (i18nLabel != null) {
            setDynamicAttribute(null, "label", getRequestContext().getMessage(i18nLabel) + ":");
            if (i18nTitle == null && getTitle() == null) {
                setTitle(getRequestContext().getMessage(i18nLabel));
            }
        }
        if (i18nTitle != null && getTitle() == null) {
            setTitle(getRequestContext().getMessage(i18nTitle));
        }

        setDynamicAttribute(null, "data-dojo-type", "dijit/form/Select");
        if (getItems() instanceof Collection) {
            Collection items = (Collection) getItems();
            if (!items.isEmpty()) {
                setItemValue("value");
                setItemLabel("label");
                List<LocalizedOption> localizedOptions = new ArrayList<>();
                for (Object o : items) {
                    if (o instanceof LocalizableEnum) {
                        final LocalizableEnum en = (LocalizableEnum) o;
                        localizedOptions.add(new LocalizedOption(getRequestContext().getMessage(en.getI18nKey()), en.getName()));
                    }
                }
                setItems(localizedOptions);
            }
        }
        return super.writeTagContent(tagWriter);
    }

    /**
     * @param i18nLabel the i18nLabel to set
     */
    public void setI18nLabel(String i18nLabel) {
        this.i18nLabel = i18nLabel;
    }

    /**
     * @param i18nTitle the i18nTitle to set
     */
    public void setI18nTitle(String i18nTitle) {
        this.i18nTitle = i18nTitle;
    }

    /**
     * @return the i18nLabel
     */
    public String getI18nLabel() {
        return i18nLabel;
    }

    /**
     * @return the i18nTitle
     */
    public String getI18nTitle() {
        return i18nTitle;
    }

}
