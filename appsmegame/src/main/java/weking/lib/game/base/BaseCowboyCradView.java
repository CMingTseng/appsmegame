package weking.lib.game.base;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import weking.lib.game.GameC;
import weking.lib.game.R;
import weking.lib.game.bean.AllBetPush;
import weking.lib.game.bean.BetRespond;
import weking.lib.game.bean.GameItemInfo;
import weking.lib.game.bean.GameKaiPaiPush;
import weking.lib.game.bean.XiaZhuPush;
import weking.lib.game.manager.GameSound;
import weking.lib.game.manager.memory.GameCowboySound;
import weking.lib.game.observer.GameObserver;
import weking.lib.game.utils.BottomBarUtil;
import weking.lib.game.utils.FastClickUtil;
import weking.lib.game.utils.GameAction;
import weking.lib.game.utils.GameAnimUtil;
import weking.lib.game.utils.GameUIUtils;
import weking.lib.game.utils.GameUtil;
import weking.lib.game.view.cardView.cowboy.CowboyBetView;
import weking.lib.game.view.fragment.CowboyAnimFragment;
import weking.lib.game.view.popWin.GameCradMenuPoPWindow;
import weking.lib.utils.BitmapUtil;
import weking.lib.utils.LogUtils;

import static weking.lib.game.utils.GameUtil.getMoney;

/**
 * 创建时间 2017/10/24.
 * 创建人 frs
 * 功能描述 牛仔的基类
 */
public abstract class BaseCowboyCradView extends BaseGameDialogLayout {
    protected GameCradMenuPoPWindow menuPopupWindow;  // 菜单的popopu
    protected int mWindowWidth;
    protected float mThreeMargin;
    protected float mTreePokerWidth;  // 桌面的width
    protected ImageView iv_poker_info_icon_left, iv_poker_info_icon_ping, iv_poker_info_icon_right; // 桌面背景
    private ImageView iv_menu;  // 菜单
    //自定义下注
    protected CowboyBetView cbv_bet_left, cbv_bet_ping, cbv_bet_right;

    protected CowboyAnimFragment mGowBoyFrament;
    // 开牌过程的时间
    protected int mKaiPaiLoadingTime;
    // 显示开牌的时间
    protected int mKaiPaiShowTime;
    // 等待下一局开牌的时间
    protected int mKaiPaiNextTime;
    // 等待开牌时间
    protected int mNextKaiPaiTiem;

    // 牌的view
    protected View v_ping_poker, v_left_poker, v_right_poker;
    //庄家
    protected ImageView iv_banker_icom;
    protected TextView tv_banker_name;
    protected TextView tv_banker_money;

    public BaseCowboyCradView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    @Override
    public void init() {
        super.init();
        mWindowWidth = GameUtil.getWindowWidth(GameObserver.getAppObserver().getApp());
        mThreeMargin = GameUtil.getDimension(R.dimen.game_djs_three_poker_margin);
        mTreePokerWidth = mWindowWidth - (mThreeMargin * 4);

        iv_menu = (ImageView) this.findViewById(R.id.iv_menu);
        IV_bet10 = (ImageView) findViewById(R.id.bet10);
        IV_bet100 = (ImageView) findViewById(R.id.bet100);
        IV_bet1000 = (ImageView) findViewById(R.id.bet1000);
        IV_bet5000 = (ImageView) findViewById(R.id.bet5000);
        tv_money = (TextView) findViewById(R.id.tv_money);
        chongzhi = findViewById(R.id.chongzhi);
        root_container = (ViewGroup) findViewById(R.id.root_container);

        card_fapai = (ImageView) findViewById(R.id.card_fapai);

        tishi = (ImageView) findViewById(R.id.tishi);
        tishi_bg = (ImageView) findViewById(R.id.tishi_bg);//null

        rl_bet_left = findViewById(R.id.rl_bet_left);
        rl_bet_ping = findViewById(R.id.rl_bet_ping);
        rl_bet_right = findViewById(R.id.rl_bet_right);
        rl_bet_banker = findViewById(R.id.bet_banker);


        bet_left_all = (TextView) rl_bet_left.findViewById(R.id.bet_all);
        bet_ping_all = (TextView) rl_bet_ping.findViewById(R.id.bet_all);
        bet_user_all = (TextView) rl_bet_right.findViewById(R.id.bet_all);

        // 应用字体
        bet_left_all.setTypeface(mTypeFace);
        bet_ping_all.setTypeface(mTypeFace);
        bet_user_all.setTypeface(mTypeFace);

        bet_left_me = (TextView) rl_bet_left.findViewById(R.id.bet_me);
        bet_ping_me = (TextView) rl_bet_ping.findViewById(R.id.bet_me);
        bet_right_me = (TextView) rl_bet_right.findViewById(R.id.bet_me);
        // 应用字体
        bet_left_me.setTypeface(mTypeFace);
        bet_ping_me.setTypeface(mTypeFace);
        bet_right_me.setTypeface(mTypeFace);

        iv_menu.setOnClickListener(this);
        if (!isPublish) {
            iv_menu.setImageResource(R.drawable.selector_game_card_buttom_menu_cards);
        }

        view_tip = this.findViewById(R.id.view_tip);
        rl_bet_left.setOnClickListener(this);
        rl_bet_ping.setOnClickListener(this);
        rl_bet_right.setOnClickListener(this);

        IV_bet10.setOnClickListener(this);
        IV_bet100.setOnClickListener(this);
        IV_bet1000.setOnClickListener(this);
        IV_bet5000.setOnClickListener(this);
        chongzhi.setOnClickListener(this);
        findViewById(R.id.crad_toolbar).setOnClickListener(this);
        findViewById(R.id.iv_banker).setOnClickListener(this);
        findViewById(R.id.rl_banker_win_info).setOnClickListener(this);
        findViewById(R.id.rl_banker).setOnClickListener(this);
        mMy_diamonds = getMoney();
        GameUIUtils.initnotityCradUI(mMy_diamonds, currentBet, IV_bet10, IV_bet100, IV_bet1000, IV_bet5000);
    }


    /**
     * 下注时间倒计时任务
     */
    Runnable timeDJSTask = new Runnable() {
        @Override
        public void run() {
            timeDJS--;
            if (timeDJS == 1) {
                // 主播进行命令开牌发送
                endGame();
            }
            if (timeDJS < 0) {
                game_state = GAME_STATE_KAI_PAI;
                mHandler.removeCallbacks(timeDJSTask);
                mGowBoyFrament.heidTime();
                return;
            }
            if (mHandler != null) {
                mHandler.postDelayed(timeDJSTask, 1000);
                mGowBoyFrament.shouDownTiem(timeDJS);
            }


        }
    };

    // 开始倒计时下注时间
    @Override
    protected void djsAction(final int timeInt) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                timeDJS = timeInt;
                LogUtils.d("  timeDJS  " + timeDJS);
                mGowBoyFrament.shouOneDownTiem(timeDJS);
                highlightBet();
                mHandler.post(timeDJSTask);
            }
        }, 300);

    }

    @Override
    public void setAllBetText(AllBetPush allBetPush) {
        super.setAllBetText(allBetPush);
        String accountText = GameObserver.getAppObserver().getStringText(GameC.str.ACCOUNT);
        String account = (String) GameObserver.getAppObserver().getObject(accountText, "");
        String toAccount = allBetPush.getAccount() + "";
        if (!TextUtils.equals(account, toAccount)) {
            switch (allBetPush.getPosition_id()) {
                case BET_LEFT:
                    cbv_bet_left.addBet(allBetPush.getBet_number());
                    break;
                case BET_CENTER:
                    cbv_bet_ping.addBet(allBetPush.getBet_number());
                    break;
                case BET_RIGHT:
                    cbv_bet_right.addBet(allBetPush.getBet_number());
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 设置我的钻石
     *
     * @param money 我的钻石
     */
    @Override
    protected void setBetImage(int money) {
        currentBet = BottomBarUtil.setBetImage(IV_bet10, IV_bet100, IV_bet1000, IV_bet5000, money, currentBet);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.chongzhi) {
            goPayListActivity();

        } else if (i == R.id.rl_bet_left) {//
            if (!checkMoney()) {
                return;
            }
            if (game_state == GAME_STATE_XIA_ZHU) {
                if (mPresenter != null) {
                    int live_id = GameObserver.getAppObserver().getInteger(GameC.str.IN_ROOM_LIVE_ID);
                    mPresenter.bet(live_id, BET_LEFT, currentBet);


                }
            }

        } else if (i == R.id.rl_bet_ping) {
            if (!checkMoney()) {
                return;
            }
            if (game_state == GAME_STATE_XIA_ZHU) {
                if (mPresenter != null) {
                    int live_id = GameObserver.getAppObserver().getInteger(GameC.str.IN_ROOM_LIVE_ID);
                    mPresenter.bet(live_id, BET_CENTER, currentBet);

                }
            }

        } else if (i == R.id.rl_bet_right) {
            if (!checkMoney()) {
                return;
            }
            if (game_state == GAME_STATE_XIA_ZHU) {
                if (mPresenter != null) {
                    int live_id = GameObserver.getAppObserver().getInteger(GameC.str.IN_ROOM_LIVE_ID);
                    mPresenter.bet(live_id, BET_RIGHT, currentBet);
                }
            }
        } else if (i == R.id.bet10) {
            setBetUI(IV_bet10, BET_10);
        } else if (i == R.id.bet100) {
            setBetUI(IV_bet100, BET_100);
        } else if (i == R.id.bet1000) {
            setBetUI(IV_bet1000, BET_1000);
        } else if (i == R.id.bet5000) {
            setBetUI(IV_bet5000, BET_5000);
        } else if (i == R.id.iv_menu) { //竞猜榜
            if (isPublish) {
                showMenuWindow(iv_menu);
            } else {
                openHistoryDialog(getGameType());
            }
        } else if (i == R.id.iv_banker) { // 庄家列表
            openBankerListDialog();
        } else if (i == R.id.rl_banker_win_info) { // 庄家流水
            openBenderInfoDialog();
        }else if (i == R.id.rl_banker) { // 庄家个人
            showOtherInfoDialog(mBanker_account);
        }
    }

    @Override
    public void betResult(BetRespond betRespond) {
        super.betResult(betRespond);
        GameCowboySound.getInstance().playSound(getContext(), GameCowboySound.GAME_COWBOY_CHIP, 0,isPublish);
        switch (betRespond.getPosition_id()) {
            case BET_LEFT:
                bet_left_me.setText(" " + betRespond.getMy_bet_number() + " ");
                showMybetAnim(cbv_bet_left, betRespond.getCurrentBet());
                break;
            case BET_CENTER:
                bet_ping_me.setText(" " + betRespond.getMy_bet_number() + " ");
                showMybetAnim(cbv_bet_ping, betRespond.getCurrentBet());
                break;
            case BET_RIGHT:
                bet_right_me.setText(" " + betRespond.getMy_bet_number() + " ");
                showMybetAnim(cbv_bet_right, betRespond.getCurrentBet());
            default:
                break;
        }
    }

    protected void showMybetAnim(final CowboyBetView cbvBet, final int bet) {

        cbvBet.addMyBet(bet, new GameAction().new Three<ImageView, Integer, Integer>() {
            @Override
            public void invoke(final ImageView imageView, Integer leftMargin, Integer topMargin) {
                ImageView IV_bet = null;

                switch (bet) {
                    case GameAnimUtil.BET_10:
                        IV_bet = IV_bet10;
                        break;
                    case GameAnimUtil.BET_100:
                        IV_bet = IV_bet100;
                        break;
                    case GameAnimUtil.BET_1000:
                        IV_bet = IV_bet1000;
                        break;
                    case GameAnimUtil.BET_5000:
                        IV_bet = IV_bet5000;
                        break;
                    default:
                        IV_bet = IV_bet10;
                        break;
                }
                GameAnimUtil.myAddBet(IV_bet, imageView, root_container, bet, leftMargin, topMargin, new GameAnimUtil.GameLiveUtilListener() {
                    @Override
                    public void animEnd() {
                        cbv_bet_left.showMyBetView(imageView);
                    }
                });
            }


        });
    }


    protected void setBetUI(ImageView iv_bet, int bet) {
        currentBet = bet;
        if (bet > mMy_diamonds) {
            GameUIUtils.showCheckMoney();
            return;
        }
        mMy_diamonds = getMoney();
        GameUIUtils.notityCradUI(mMy_diamonds, currentBet, IV_bet10, IV_bet100, IV_bet1000, IV_bet5000);
//        }
    }

    protected void showMenuWindow(ImageView iv_menu) {
        if (FastClickUtil.isFastClick(500)) {
            return;
        }
        if (menuPopupWindow == null) {
            // 创建一个PopuWidow对象
            menuPopupWindow = new GameCradMenuPoPWindow(GameObserver.getAppObserver().getCurrentActivity());
        }
        menuPopupWindow.setGameCardMenuListener(new GameCradMenuPoPWindow.GameCardMenuListener() {
            @Override
            public void OnClickWinningRecord() {
                openBetDialog();
            }

            @Override
            public void OnClickCardFapai() {
                openHistoryDialog(getGameType());
            }
        });
        menuPopupWindow.showPopupWindow(iv_menu);
    }


    /**
     * 盖牌
     *
     * @param targetView
     */
    @Override
    protected void cardBackVisible(View targetView) {
        targetView.setVisibility(VISIBLE);
        int pokerBgRes = GameUIUtils.getPokerRes(getGameType());
        if (pokerBgRes != 0) {
            ((ImageView) targetView.findViewById(R.id.iv_bg)).setImageResource(pokerBgRes);
        }
        targetView.findViewById(R.id.iv_bg).setVisibility(VISIBLE);
    }

    /**
     * 翻牌
     *
     * @param targetView
     */
    @Override
    protected void cardVisible(View targetView) {
        targetView.setVisibility(VISIBLE);
        targetView.findViewById(R.id.iv_bg).setVisibility(GONE);
    }

    @Override
    protected void initAPokerCode(View card, int codeRes, int r, int g, int b,
                                  int huaseResSm, int huaseBig) {
        ImageView iv_pokercode = (ImageView) card.findViewById(R.id.iv_pokercode);
        ImageView iv_pokerSizesl = (ImageView) card.findViewById(R.id.iv_pokerSizesl);
        ImageView iv_pokerSizebig = (ImageView) card.findViewById(R.id.iv_pokerSizesm);
        iv_pokerSizesl.setImageResource(huaseResSm);
        iv_pokerSizebig.setImageResource(huaseBig);
        iv_pokercode.setImageBitmap(BitmapUtil.changeColor(mApp, codeRes, r, g, b));

    }

    @Override
    protected void showFapaiCard() {
        if (isPublish) {
            // 发牌的背景
            int pokerRes = GameUIUtils.getPokerRes(getGameType());
            if (pokerRes != 0) {
                card_fapai.setImageResource(pokerRes);
            }

        }
    }

    /**
     * 游戏开始请稍后
     */
    @Override
    protected void showTishiImg(String tishiType) {
        int imgRes = 0;
        if (TextUtils.equals(tishiType, GameC.img.game_yxjjks)) {
            imgRes = R.drawable.game_yxjjks;
        } else if (TextUtils.equals(tishiType, GameC.img.game_jyxcbz)) {
            imgRes = R.drawable.game_jyxcbz;
        } else if (TextUtils.equals(tishiType, GameC.img.game_bpks)) {
            imgRes = R.drawable.game_bpks;
        } else if (TextUtils.equals(tishiType, GameC.img.game_xzxyqy)) {
            imgRes = R.drawable.game_xzxyqy;
        }


        if (imgRes != 0) {
            tishi.setImageResource(imgRes);
        }

    }

    @Override
    public void doKaiPai(GameKaiPaiPush gamePush, GameSound gameSound) {
        super.doKaiPai(gamePush, gameSound);
        try {
            mBanker_win_num = gamePush.getBanker_win_num();
            mBanker_diamond = gamePush.getBanker_diamond();
//            tv_banker_money.setText(GameUtil.formatBetText(mBanker_diamond));

//            this.mHandler.postDelayed(new Runnable() {
//                @Override
//                pu   blic void run() {
//                    game_state = 2;
//                    tipKaiPai();
//                }
//            }, 1000L);
            mKaiPaiLoadingTime = gamePush.getCountdown_time() / 3 * 1000;
            mKaiPaiShowTime = gamePush.getCountdown_time() / 3 * 1000;
            mKaiPaiNextTime = gamePush.getCountdown_time() / 3 * 1000;
            mNextKaiPaiTiem = mKaiPaiLoadingTime * 4 / 10;
            final List<GameItemInfo> gameInfo = gamePush.getGame_info();
            if (gameInfo.size() != 4) {
                GameObserver.getAppObserver().showLog("BaseGameLayout", "服务器返回的牌错误；gameInfo数不是3");
            } else {
                initPokerInfo(gameInfo);

                this.mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        kaiPai(gameInfo);
                    }
                }, mNextKaiPaiTiem);
            }
        } catch (Exception e) {
            GameObserver.getAppObserver().showLog("BaseGameLayout", e.getMessage());
            e.printStackTrace();
        } finally {
            doStartGameAction(gamePush.getCountdown_time());
            nextGame(gamePush.getCountdown_time() - 7);
        }
    }

    int newTime = 5;
    private Runnable nextGameRunnable = new Runnable() {
        @Override
        public void run() {
            if (newTime == 5) {
                mGowBoyFrament.shouOneNextGame(newTime);
                newTime--;
                mHandler.postDelayed(nextGameRunnable, 1000);
            } else if (newTime < 0) {
                mHandler.removeCallbacks(nextGameRunnable);
                mGowBoyFrament.heidTime();
                newTime = 5;
            } else {
                mGowBoyFrament.shouNextGame(newTime);
                newTime--;
                mHandler.postDelayed(nextGameRunnable, 1000);
            }
        }
    };


    protected void nextGame(int tiem) {
        mHandler.postDelayed(nextGameRunnable, tiem * 1000);
    }

    /**
     * 显示牌的数据
     *
     * @param gameInfo
     */
    @Override
    protected abstract void kaiPai(List<GameItemInfo> gameInfo);


    /**
     * 设置开牌数据
     *
     * @param gameInfo
     */
    @Override
    protected abstract void initPokerInfo(List<GameItemInfo> gameInfo);


    protected void faPaiAnimAll(int time) {

        if (isPublish) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    card_fapai.setVisibility(INVISIBLE);
                }
            }, time);
        } else {
            card_fapai.setVisibility(VISIBLE);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    card_fapai.setVisibility(INVISIBLE);
                }
            }, time + 500);

        }
    }

    @Override
    public void doXiaZhu(final GameSound gameSound, XiaZhuPush xiaZhuPush) {
        beforeNext();

        this.game_state = 3;
    }

    /**
     * 赢牌高亮牌
     */
    protected void highlightPoker() {
        v_left_poker.bringToFront();
        v_ping_poker.bringToFront();
        v_right_poker.bringToFront();
    }

    /**
     * 赢牌高亮下注
     */
    protected void highlightBet() {
        cbv_bet_left.bringToFront();
        cbv_bet_ping.bringToFront();
        cbv_bet_right.bringToFront();
    }

}
