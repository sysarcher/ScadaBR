package br.org.scadabr.l10n;

import br.org.scadabr.web.i18n.LocalizableMessage;
import com.serotonin.mango.rt.dataImage.types.MangoValue;
import com.serotonin.mango.vo.DataPointVO;
import java.util.Locale;

/**
 * This interface localizes I18N keys and messages with te apropirate MessageSource@MessageBundle and Locale and TimeZone
 *
 */
public interface Localizer  {
    
    String localizeTimeStamp(long ts, boolean hideDateOfToday);

    String localizeDate(long ts);

    String localizeTime(long ts);
    
    String getMessage(String code, Object ... args);
    
    String getMessage(LocalizableMessage localizableMessage);

    Locale getLocale();
    
    String format(DataPointVO dpVo, MangoValue mv);
    
    String formatOnlyValue(DataPointVO dpVo, MangoValue mv);
}
