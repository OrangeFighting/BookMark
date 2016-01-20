package com.mclab.lcc.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;

import com.mclab.lcc.bookmark.R;
import com.mclab.lcc.component.TagCloudView;
import com.mclab.lcc.component.TagView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Administrator on 2015/5/12.
 */
public class OneDayTagsActivity extends Activity{
    private TagCloudView mTagCloudView;
    private Bundle m_tagsBundle;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_tagsBundle = getIntent().getExtras().getBundle("tagsBundle");


        //将Bundle数据传入createTags()处理，获取Tags列表（还是乱序的，但是有counts标记）
        // Step0: to get a full-screen View:
        /*requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);*/

        // Step1: get screen resolution:
        final Display display = getWindowManager().getDefaultDisplay();
        final int width = display.getWidth();
        final int height = display.getHeight();

        // Step2: create the required TagList:
        // notice: All tags must have unique text field
        // if not, only the first occurrence will be added and the rest will be
        // ignored
        final List<TagView> myTagList = createTags();

        // Step3: create our TagCloudview and set it as the content of our
        // MainActivity
        mTagCloudView = new TagCloudView(this, width, height, myTagList); // passing
        // current
        // context
        setContentView(mTagCloudView);
        mTagCloudView.requestFocus();
        mTagCloudView.setFocusableInTouchMode(true);

        // Step4: (Optional) adding a createTag and resetting the whole 3D
        // TagCloud
        // you can also add individual tags later:
        // mTagCloudView.addTag(createTag("AAA", 5, "http://www.aaa.com"));
        // .... (several other tasg can be added similarly )
        // indivual tags will be placed along with the previous tags without
        // moving
        // old ones around. Thus, after adding many individual tags, the
        // TagCloud
        // might not be evenly distributed anymore. reset() re-positions all the
        // tags:
        // mTagCloudView.reset();

        // Step5: (Optional) Replacing one of the previous tags with a createTag
        // you have to create a newTag and pass it in together
        // with the Text of the existing Tag that you want to replace
        // Tag newTag=createTag("Illinois", 9, "http://www.illinois.com");
        // in order to replace previous tag with text "Google" with this new
        // one:
        // boolean result=mTagCloudView.Replace(newTag, "google");
        // result will be true if "google" was found and replaced. else result
        // is false
    }
    //接收Tags 的Map数据
    //返回TagView的列表
    //颜色随机给予
    private List<TagView> createTags() {
        // create the list of tags with popularity values and related url
        List<TagView> tempList = new ArrayList<TagView>();

        Set<String> keyset = m_tagsBundle.keySet();
        int i =0;//颜色控制
        int color ;
        for(String label:keyset)
        {
            i++;
            if(i%3==1)color = getColor(R.color.view_tag_red);
            else if(i%3==2)color = getColor(R.color.view_tag_green);
            else color = getColor(R.color.view_tag_blue);
            int count = m_tagsBundle.getInt(label);
            tempList.add(createTag(label,count,color));
        }
        return tempList;
    }
   /*//此函数应该并入createTags中
    private List<TagView> decodeTags(final int resId, final int color) {
        final List<TagView> tags = new ArrayList<TagView>();

        final String[] labels = getResources().getStringArray(resId);
        for (int index = 0; index < labels.length; index++) {
            final String label = labels[index];

            tags.add(createTag(label, labels.length - index, color));
        }

        return tags;
    }*/

    private TagView createTag(final String text, final int popularity,
                              final int color) {
        final TagView.TagBundle bundle = new TagView.TagBundle(text, popularity, color);
        return new TagView(this, bundle);
    }

    private int getColor(final int colorRes) {
        return getResources().getColor(colorRes);
    }
}
