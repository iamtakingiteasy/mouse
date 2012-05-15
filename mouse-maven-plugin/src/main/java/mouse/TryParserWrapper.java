package mouse;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Alexander <itakingiteasy> Tumin
 * Created: 2012-05-15 21:18
 */
public class TryParserWrapper extends TryParser {
    public void run(String[] args) throws InvocationTargetException, IllegalAccessException, IOException, ClassNotFoundException, NoSuchMethodException, InstantiationException {
        if (!init(args)) return;

        if (files==null)
            interact();

        else
            for (String name: files)
                run(name);
    }
}
