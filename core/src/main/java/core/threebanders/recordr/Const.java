package core.threebanders.recordr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Const {

    public static final String DATABASE_NAME = "call_recorder.db";
    public static final String SPEAKER_USE = "put_on_speaker";

    public static final List<CoreUtil.PhoneTypeContainer> PHONE_TYPES = new ArrayList<>(Arrays.asList(
            new CoreUtil.PhoneTypeContainer(1, Core.getContext().getString(R.string.home)),
            new CoreUtil.PhoneTypeContainer(2, Core.getContext().getString(R.string.mobile)),
            new CoreUtil.PhoneTypeContainer(3, Core.getContext().getString(R.string.work)),
            new CoreUtil.PhoneTypeContainer(-1, Core.getContext().getString(R.string.unknown)),
            new CoreUtil.PhoneTypeContainer(7, Core.getContext().getString(R.string.other))
    ));
}
