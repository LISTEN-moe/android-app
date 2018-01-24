package me.echeung.moemoekyun.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;

public final class BlurUtil {

    private static RenderScript rs;

    // See https://gist.github.com/boxme/5079826746987c000ef8
    public static Bitmap blur(Context context, Bitmap originalBitmap, float strength) {
        RenderScript rs = getRenderScript(context);

        // Creates a matching Renderscript allocation object and
        // copies the contents of the bitmap into the allocation
        Allocation input = Allocation.createFromBitmap(rs, originalBitmap,
                Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);

        // Generates an Allocation identical in structure to the first
        Allocation output = Allocation.createTyped(rs, input.getType());

        // Uses Renderscript ScriptIntrinsicBlur, a Gaussian blur filter
        ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));

        // Control the strength of the blur
        script.setRadius(strength);

        script.setInput(input);

        // Blur
        script.forEach(output);

        // Copy the blurred image back to Java memory space
        output.copyTo(originalBitmap);

        return originalBitmap;
    }

    public static RenderScript getRenderScript(Context context) {
        if (rs == null) {
            rs = RenderScript.create(context);
        }
        return rs;
    }
}
