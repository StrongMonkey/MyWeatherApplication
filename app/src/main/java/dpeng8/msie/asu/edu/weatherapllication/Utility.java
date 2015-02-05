package dpeng8.msie.asu.edu.weatherapllication;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Copyright 2015 Daishan Peng
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * Created by monkey on 1/31/2015.
 * <p/>
 * The instuctor and TA have the right to build and evaluate the software package.
 *
 * @Author Daishan Peng   mailto:dpeng8@asu.edu
 * @Version January 15 2015
 */
public class Utility {
    /*
    This method is used to get the location from the preference setting
     */
    public static String getPreferredLocation(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.Location_key),
                context.getString(R.string.default_location));
    }
}
