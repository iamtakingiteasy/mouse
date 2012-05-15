package mouse;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Alexander <itakingiteasy> Tumin
 * Created: 2012-05-15 21:37
 */
public class TestParserWrapper extends TestParser {
    public void run(String[] args) throws InvocationTargetException, IllegalAccessException, ClassNotFoundException, IOException, NoSuchMethodException, InstantiationException {
        if (!init(args)) return;

        if (files==null)
            interact();

        else
            for (String name: files)
                run(name);
    }
}
