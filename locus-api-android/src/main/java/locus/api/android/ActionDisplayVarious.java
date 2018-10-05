package locus.api.android;

import android.content.Context;
import android.content.Intent;

import java.util.List;

import locus.api.android.utils.LocusConst;
import locus.api.android.utils.LocusUtils.LocusVersion;
import locus.api.android.utils.LocusUtils.VersionCode;
import locus.api.android.utils.exceptions.RequiredVersionMissingException;
import locus.api.objects.Storable;
import locus.api.objects.extra.Circle;

public class ActionDisplayVarious extends ActionDisplay {

    /**
     * Simple way how to send circles over intent to Locus.
     *
     * @return true if success
     */
    public static boolean sendCirclesSilent(Context context,
            List<Circle> circles, boolean centerOnData)
            throws RequiredVersionMissingException {
        return sendCirclesSilent(LocusConst.ACTION_DISPLAY_DATA_SILENTLY,
                context, circles, false, centerOnData);
    }

    private static boolean sendCirclesSilent(String action, Context context,
            List<Circle> circles, boolean callImport, boolean centerOnData)
            throws RequiredVersionMissingException {
        // check data
        if (circles == null || circles.size() == 0) {
            return false;
        }

        // create and start intent
        Intent intent = new Intent();
        intent.putExtra(LocusConst.INTENT_EXTRA_CIRCLES_MULTI,
                Storable.getAsBytes(circles));
        return sendData(action, context, intent, callImport, centerOnData,
                VersionCode.UPDATE_02);
    }

    /**
     * Allow to remove visible circles defined by it's ID value
     *
     * @param ctx     current context that send broadcast
     * @param itemsId list of circles IDs that should be removed from map
     */
    public static void removeCirclesSilent(Context ctx, LocusVersion lv, long[] itemsId)
            throws RequiredVersionMissingException {
        removeSpecialDataSilently(ctx, lv, LocusConst.INTENT_EXTRA_CIRCLES_MULTI, itemsId);
    }
}
