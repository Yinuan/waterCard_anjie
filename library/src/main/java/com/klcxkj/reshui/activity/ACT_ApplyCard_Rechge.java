package com.klcxkj.reshui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.klcxkj.klcxkj_waterdemo.R;
import com.klcxkj.reshui.RechagePayBak;
import com.klcxkj.reshui.entry.BaseBo;
import com.klcxkj.reshui.entry.CardInfo;
import com.klcxkj.reshui.entry.OrderInfo;
import com.klcxkj.reshui.entry.UserInfo;
import com.klcxkj.reshui.tools.StringConfig;
import com.klcxkj.reshui.util.AppPreference;
import com.klcxkj.reshui.util.GlobalTools;
import com.klcxkj.reshui.widget.LoadingDialogProgress;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;



public class ACT_ApplyCard_Rechge extends ACT_Network {

    private TextView content;
    private Button btn;
    private UserInfo userInfo;
    private CardInfo mCardInfo;
    private LoadingDialogProgress progress;
    private String urlGetAlipayInfor = StringConfig.BASE_URL+"tPrjInfo/getAlipayInfor2?";

    private OrderInfo mOrderInfo;
    private String urlReturnUrlApp;
    private static final int SDK_PAY_FLAG = 1;
    private static final int SDK_CHECK_FLAG = 2;

    private String perMonney;
    private static RechagePayBak payBak;

    public static void setPayBak(RechagePayBak payBak) {
        ACT_ApplyCard_Rechge.payBak = payBak;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act__apply_card__rechge);
        initData();
        initview();
        bindView();
    }

    private void initData() {
        mCardInfo = AppPreference.getInstance().getCardInfo();
        userInfo =AppPreference.getInstance().getUserInfo();
    }

    private void bindView() {
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("PrjID", userInfo.getPrjID()+"");
                params.put("EmployeeName",mCardInfo.getEmployeeName());
                params.put("EmployeeID", mCardInfo.getEmployeeID()+"");
                params.put("CardID", mCardInfo.getCardID()+"");
                //perMonney
                params.put("totalFee",perMonney);
                params.put("ServerIP", userInfo.getServerIP());
                params.put("ServerPort", userInfo.getServerPort()+"");
                sendPostRequest(urlGetAlipayInfor, params);
                btn.setEnabled(false);
                progress = GlobalTools.getInstance().showDailog(ACT_ApplyCard_Rechge.this,"准备中..");
            }
        });
    }

    private void initview() {
        showMenu("预充值");
        Intent in =getIntent();
        perMonney =in.getStringExtra("perMonny");
        content = (TextView) findViewById(R.id.apply_card_rechge_content);
        btn = (Button) findViewById(R.id.apply_card_rechge_btn);
        content.setText("您已成功提交申请，本业务需预充"+perMonney+"元，才可领卡。");
    }


    @Override
    protected void handleErrorResponse(String url, VolleyError error) {
        super.handleErrorResponse(url, error);
        progress.dismiss();
        if(error instanceof TimeoutError){
            toast(R.string.timeout_error);
        }else{
            toast(R.string.operate_error);
        }
        btn.setEnabled(true);
    }



    @Override
    protected void handleResponse(String url, JSONObject json) {
        super.handleResponse(url, json);
        progress.dismiss();
        BaseBo result = new Gson().fromJson(json.toString(), BaseBo.class);
        if(result.isSuccess()){
            if (url.contains(urlGetAlipayInfor)) {
                Gson gson = new Gson();
                mOrderInfo = gson.fromJson(json.toString(), OrderInfo.class);
                urlReturnUrlApp = mOrderInfo.getReturn_url();
                payBak.payBack(mOrderInfo.getOrderdes(),"cardGet",perMonney);
            }else if (url.contains(urlReturnUrlApp)){
                //toast(result.getMsg());
            }
        }else{
            try {
                toast(json.getString("msg"));
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        btn.setEnabled(true);
    }



    private HashMap<String, String> getParams(String result) {
        HashMap<String, String> params = new HashMap<String, String>();
        String[] ccc = result.split("&");
        for (String string2 : ccc) {
            String[] ddd = string2.split("=");
            if (ddd.length > 1) {
                params.put(ddd[0], ddd[1].replace("\"", ""));
            }
        }
        return params;
    }
    /**
     * check whether the device has authentication alipay account.
     * 查询终端设备是否存在支付宝认证账户
     *
     */
    public void check(View v) {

    }

    /**
     * get the sdk version. 获取SDK版本号
     *
     */
    public void getSDKVersion() {

    }

    /**
     * create the order info. 创建订单信息
     *
     */
    public String getOrderInfo(String subject, String body, String price) {
        // 签约合作者身份ID
        String orderInfo = "partner=" + "\"" + mOrderInfo.getPartner()+ "\"";

        // 签约卖家支付宝账号
        orderInfo += "&seller_id=" + "\"" + mOrderInfo.getSeller_email() + "\"";

        // 商户网站唯一订单号
        orderInfo += "&out_trade_no=" + "\"" + mOrderInfo.getOrderID() + "\"";   //getOutTradeNo()

        // 商品名称
        orderInfo += "&subject=" + "\"" + subject + "\"";

        // 商品详情
        orderInfo += "&body=" + "\"" + body + "\"";

        // 商品金额
        orderInfo += "&total_fee=" + "\"" + price + "\"";

        // 服务器异步通知页面路径
        orderInfo += "&notify_url=" + "\"" + mOrderInfo.getNotify_url() + "\"";

//		orderInfo += "&notify_url=" + "\"" + "http://notify.msp.hk/notify.htm"
//				+ "\"";
        // 服务接口名称， 固定值
        orderInfo += "&service=\"mobile.securitypay.pay\"";

        // 支付类型， 固定值
        orderInfo += "&payment_type=\"1\"";

        // 参数编码， 固定值
        orderInfo += "&_input_charset=\"utf-8\"";

        // 设置未付款交易的超时时间
        // 默认30分钟，一旦超时，该笔交易就会自动被关闭。
        // 取值范围：1m～15d。
        // m-分钟，h-小时，d-天，1c-当天（无论交易何时创建，都在0点关闭）。
        // 该参数数值不接受小数点，如1.5h，可转换为90m。
        orderInfo += "&it_b_pay=\"30m\"";

        // extern_token为经过快登授权获取到的alipay_open_id,带上此参数用户将使用授权的账户进行支付
        // orderInfo += "&extern_token=" + "\"" + extern_token + "\"";

        // 支付宝处理完请求后，当前页面跳转到商户指定页面的路径，可空
        orderInfo += "&return_url=\"m.alipay.com\"";

        // 调用银行卡支付，需配置此参数，参与签名， 固定值 （需要签约《无线银行卡快捷支付》才能使用）
        // orderInfo += "&paymethod=\"expressGateway\"";

        return orderInfo;
    }
}
