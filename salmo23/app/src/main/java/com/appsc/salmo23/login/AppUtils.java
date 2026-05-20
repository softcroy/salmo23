package com.appsc.salmo23.login;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.List;

public class AppUtils {

    public static List<String> getInstalledPackageNames(Context context) {
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> packages = pm.getInstalledPackages(0);
        List<String> packageNames = new ArrayList<>();
        for (PackageInfo pkgInfo : packages) {
            // Filtrar apenas apps de usuário (opcional)
            if ((pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                packageNames.add(pkgInfo.packageName);
            }
        }
        return packageNames;
    }
}
