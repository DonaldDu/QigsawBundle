package com.dhy.compatbundle;


import androidx.annotation.Keep;

import java.util.List;
import java.util.Set;

@Keep
public class SplitDetails {
    public String qigsawId;
    public String appVersionName;
    public List<String> updateSplits;
    public Set<String> splitEntryFragments;
    public List<SplitInfo> splits;
}

