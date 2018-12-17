package com.qubin.downloadmanager;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

/**
 * 类或接口的描述信息
 *
 * @Author:qubin
 * @Theme: 通用Dialog
 * @Data:2018/12/17
 * @Describe:
 */
public class CommonDialog extends Dialog {


    private boolean cancelTouchout;
    private View view;

    public CommonDialog(Builder builder) {
        super(builder.context);

        cancelTouchout = builder.cancelTouchout;
        view = builder.view;
    }

    public CommonDialog(Builder builder, int resStyle) {
        super(builder.context, resStyle);

        cancelTouchout = builder.cancelTouchout;
        view = builder.view;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(view);
        setCancelable(cancelTouchout);
        setCanceledOnTouchOutside(cancelTouchout);
    }

    public static final class Builder{
        private Context context;
        private boolean cancelTouchout;
        private View view;
        private int resStyle = -1;

        public Builder(Context context){
            this.context = context;
        }

        public Builder view(int resView){
            view = LayoutInflater.from(context).inflate(resView,null);
            return this;
        }

        public Builder style(int resStyle){
            this.resStyle = resStyle;
            return this;
        }

        public Builder cancelTouchout(boolean val){
            cancelTouchout = val;
            return this;
        }

        public Builder addViewOnClick(int viewRes,View.OnClickListener listener){
            view.findViewById(viewRes).setOnClickListener(listener);
            return this;
        }

        public Builder setTitle(int viewRes,String title){
            TextView textTitle = (TextView)view.findViewById(viewRes);
            textTitle.setText(title);
            return this;
        }

        public Builder setMessage(int viewRes,String message){
            TextView txtMessage = (TextView)view.findViewById(viewRes);
            txtMessage.setText(message);
            return this;
        }

        public Builder setVersion(int viewVersion,String newVersion){
            TextView txtMessage = (TextView)view.findViewById(viewVersion);
            txtMessage.setText("发现新版本" + newVersion);
            return this;
        }

        public CommonDialog build(){
            if (resStyle != -1){
                return new CommonDialog(this,resStyle);
            }else {
                return new CommonDialog(this);
            }
        }


    }
}
