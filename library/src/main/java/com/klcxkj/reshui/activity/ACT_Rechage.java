package com.klcxkj.reshui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.klcxkj.klcxkj_waterdemo.R;
import com.klcxkj.reshui.RechagePayBak;
import com.klcxkj.reshui.adpater.GRechangeAdapter;
import com.klcxkj.reshui.entry.BaseBo;
import com.klcxkj.reshui.entry.CardInfo;
import com.klcxkj.reshui.entry.OrderInfo;
import com.klcxkj.reshui.entry.RechangeValue;
import com.klcxkj.reshui.entry.UserInfo;
import com.klcxkj.reshui.tools.StringConfig;
import com.klcxkj.reshui.util.AppPreference;
import com.klcxkj.reshui.util.GlobalTools;
import com.klcxkj.reshui.widget.LoadingDialogProgress;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * author : yinjuan
 * time： 2017/6/9 13:54
 * email：yin.juan2016@outlook.com
 * Description:充值
 */
public class ACT_Rechage extends ACT_Network{

	private String urlGetAlipayInfor = StringConfig.BASE_URL+"tPrjInfo/getAlipayInfor2?";

	private String urlReturnUrlApp;
	private static final int SDK_PAY_FLAG = 1;
	private static final int SDK_CHECK_FLAG = 2;
	private CardInfo mCardInfo;
	private UserInfo userInfo;
	private OrderInfo mOrderInfo;
	private GRechangeAdapter rAdapter;


	private TextView edit_cardID, edit_user_name, edit_user_balance;
	private TextView prefillMoney;//未领金额
	private String edit_money ="30元";  //充值金额
	private Button button_bind_next;
	private GridView gridView;
	private List<RechangeValue> data;
	private static final int OTHER_VALUE=-1;
	private LoadingDialogProgress progress;

	private static RechagePayBak payBak;

	public static void setPayBak(RechagePayBak payBak) {
		ACT_Rechage.payBak = payBak;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_rechage);
		initView();
		bindEvent();
		initDataFromIntent();
	}
	private void initDataFromIntent() {

		mCardInfo = AppPreference.getInstance().getCardInfo();
		userInfo =AppPreference.getInstance().getUserInfo();
		if (mCardInfo!=null){
			prefillMoney.setText(mCardInfo.getAccountMoney()+"");
			edit_cardID.setText(mCardInfo.getCardID()+"");
			edit_user_name.setText(mCardInfo.getEmployeeName());
			edit_user_balance.setText(getAmount(mCardInfo.getNCardValue()+""));  //卡余额
		}

	}
	
	private void initView() {

		button_bind_next = (Button) this.findViewById(R.id.button_bind_next);
		edit_cardID = (TextView) this.findViewById(R.id.edit_cardID);
		edit_user_name = (TextView) this.findViewById(R.id.edit_user_name);
		edit_user_balance = (TextView) this.findViewById(R.id.edit_user_balance);
		prefillMoney = (TextView) findViewById(R.id.rechange_monney_none);
		//菜单
		TextView title = (TextView) findViewById(R.id.top_title);
		TextView rihgt_txt = (TextView) findViewById(R.id.top_right_text);
		title.setText("充值");
		rihgt_txt.setText("充值记录");
		rihgt_txt.setVisibility(View.VISIBLE);
		LinearLayout backBtn = (LinearLayout) findViewById(R.id.top_btn_back);
		backBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				finish();
			}
		});
		rihgt_txt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				startActivity(new Intent(ACT_Rechage.this,ACT_Rechage_Recording.class));
			}
		});
		//适配器
		gridView = (GridView) findViewById(R.id.gridview_rechange);
		data =new ArrayList<>();
		rAdapter =new GRechangeAdapter(ACT_Rechage.this);
		data.add(new RechangeValue("30元","1",0));
		data.add(new RechangeValue("50元","0",0));
		data.add(new RechangeValue("100元","0",0));
		data.add(new RechangeValue("200元","0",0));
		data.add(new RechangeValue("300元","0",0));
		data.add(new RechangeValue("其他金额","0",-1));
		rAdapter.setList(data);
		gridView.setAdapter(rAdapter);

	}

	private String monneyValue;
	private void bindEvent() {

		//冲值按钮
		button_bind_next.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {

				Log.d("ACT_Rechage", "冲值金额：="+edit_money);
				 monneyValue =edit_money.substring(0,edit_money.length()-1);
				Log.d("ACT_Rechage", monneyValue);
				Log.d("ACT_Rechage", "monney=="+monneyValue);
				HashMap<String, String> params = new HashMap<String, String>();
				params.put("PrjID", userInfo.getPrjID()+"");
				params.put("EmployeeName",mCardInfo.getEmployeeName());
				params.put("EmployeeID", mCardInfo.getEmployeeID()+"");
				params.put("CardID", mCardInfo.getCardID()+"");
				//monneyValue
				params.put("totalFee", monneyValue);
				params.put("ServerIP", userInfo.getServerIP());
				params.put("ServerPort", userInfo.getServerPort()+"");
				sendPostRequest(urlGetAlipayInfor, params);
				button_bind_next.setEnabled(false);
				progress = GlobalTools.getInstance().showDailog(ACT_Rechage.this,"准备中..");
				Log.d("ACT_Rechage", "params:" + params);
			}
		});

		gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

				//1.显示界面的变化
				if (data.get(position).getIsCheck().equals("0")){
					for (int i = 0; i < data.size(); i++) {
						data.get(i).setIsCheck("0");
					}
					data.get(position).setIsCheck("1");
				}else {
					//data.get(position).setIsCheck("0");
				}
				edit_money =data.get(position).getValue();
				rAdapter.notifyDataSetChanged();
				//2.选择其他金额的变化
				if (data.get(position).getIsOthers()==OTHER_VALUE){
					showpop();
				}


			}
		});
		

	}


	@Override
	protected void handleErrorResponse(String url, VolleyError error) {
		super.handleErrorResponse(url, error);
		Log.d("ACT_Rechage", "error:======"+error.toString());
		progress.dismiss();
		if(error instanceof TimeoutError){
    		toast(R.string.timeout_error);
    	}else{
    		toast(R.string.operate_error);
    	}
		button_bind_next.setEnabled(true);
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
				payBak.payBack(mOrderInfo.getOrderdes(),"cardMonney",monneyValue);

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
		button_bind_next.setEnabled(true);
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

	/**
	 * get the out_trade_no for an order. 生成商户订单号，该值在商户端应保持唯一（可自定义格式规范）
	 * 
	 */
	public String getOutTradeNo() {
		SimpleDateFormat format = new SimpleDateFormat("MMddHHmmss",
				Locale.getDefault());
		Date date = new Date();
		String key = format.format(date);

		Random r = new Random();
		key = key + r.nextInt();
		key = key.substring(0, 15);
		return key;
	}
	public String getAmount(String amount) {
		if (amount == null) {
			amount = "0";
		}
		DecimalFormat nf = new DecimalFormat("#,##0.00");

		String amountFormat = nf.format(Double.parseDouble(amount));
		return amountFormat;
	}

	/**
	 * 选择其他金额
	 */
	private void showpop() {
		View view = LayoutInflater.from(ACT_Rechage.this).inflate(R.layout.pop_style_4, null);
		final AutoCompleteTextView value = (AutoCompleteTextView) view.findViewById(R.id.pop_4_value);
		Button btn_ok = (Button) view.findViewById(R.id.pop_4_confrim);
		Button btn_cancle = (Button) view.findViewById(R.id.pop_4_cancle);

		final PopupWindow popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		ColorDrawable cd = new ColorDrawable(0x000000);
		popupWindow.setBackgroundDrawable(cd);
		WindowManager.LayoutParams lp=getWindow().getAttributes();
		lp.alpha = 0.4f;
		getWindow().setAttributes(lp);
		//注意  要是点击外部空白处弹框消息  那么必须给弹框设置一个背景色  不然是不起作用的
		// 设置允许在外点击消失
		popupWindow.setOutsideTouchable(false);
		popupWindow.setBackgroundDrawable(new BitmapDrawable());
		//点击外部消失
		//  popupWindow.setOutsideTouchable(true);
		//设置可以点击
		popupWindow.setFocusable(true);
		// 设置背景，这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
		popupWindow.setBackgroundDrawable(new BitmapDrawable());
		// 软键盘不会挡着popupwindow
		popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				final InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.toggleSoftInput(1000, InputMethodManager.HIDE_NOT_ALWAYS);
			}
		},50);
		//popupWindow.showAsDropDown(mSubmit);
		btn_cancle.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				popupWindow.dismiss();
				//参数：1，自己的EditText。2，时间。
			}
		});
		btn_ok.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (value.getText() ==null ||value.getText().length() ==0){
					Toast.makeText(ACT_Rechage.this, "请输入金额", Toast.LENGTH_SHORT).show();
					return;
				}else if (Integer.valueOf(value.getText().toString())<5){
					toast("充值最低金额5元");
					return;
				}
				popupWindow.dismiss();
				//
				data.get(5).setValue(value.getText().toString()+"元");
				edit_money =value.getText().toString()+"元";
				rAdapter.notifyDataSetChanged();
			}
		});
		// 监听菜单的关闭事件
		popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
			@Override
			public void onDismiss() {
			}
		});
		// 监听触屏事件
		popupWindow.setTouchInterceptor(new View.OnTouchListener() {
			public boolean onTouch(View view, MotionEvent event) {
				return false;
			}
		});
		popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

			//在dismiss中恢复透明度
			public void onDismiss() {
				WindowManager.LayoutParams lp = getWindow().getAttributes();
				lp.alpha = 1f;
				getWindow().setAttributes(lp);
			}
		});

		//设置软件盘不挡
		popupWindow.setSoftInputMode(PopupWindow.INPUT_METHOD_NEEDED);
		popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

	}

}
