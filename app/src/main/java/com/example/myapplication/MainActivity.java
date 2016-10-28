package com.example.myapplication;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MainActivity extends Activity {

    private ImageView[][]iv_game_arr=new ImageView[3][5];    //利用二维数组创建小方块
    private GridLayout gl_game_main;
    private ImageView iv_game_null;             //空方块实例的保存

    private GestureDetector gestureDetector;        //手势检测
    private boolean isGameStart=false;            //判断游戏是否开始，是为了当随机移动时也会判断游戏是否结束，为了防止一开始就直接报游戏结束
    private boolean isAnimRun=false;            //当前移动动画是否正在执行，，在执行时点击是无法移动的
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        return gestureDetector.onTouchEvent(event);         //界面监听换成自己的手势监听
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {     //作用是将touch事件向下传递直到遇到被触发的目标view,如果返回true,表示当前view就是目标view,事件停止向下分发
        gestureDetector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initGesture();  //初始化手势
        setContentView(R.layout.activity_main);

        Bitmap bm_source=((BitmapDrawable)getResources().getDrawable(R.mipmap.half)).getBitmap();
                        //位图，获取
        int pic_width=bm_source.getWidth()/5;     //每个图片小方块的宽（）和高
        int src_width=getWindowManager().getDefaultDisplay().getWidth()/5;      //包装图片小方块的宽高应该是整个屏幕的宽高
    /**初始化游戏的若干个小方块，绑定数据源       */
        for(int i=0;i<iv_game_arr.length;i++){                  //初始化游戏的若干个小方块
            for(int j=0;j<iv_game_arr[0].length;j++){           //小方块的列数
                Bitmap bm_diamond=Bitmap.createBitmap(bm_source,j*pic_width,
                        i*pic_width,pic_width,pic_width);    //图片裁剪，（图片源，起始x、y位置，宽高）
                iv_game_arr[i][j]=new ImageView(this);
                iv_game_arr[i][j].setImageBitmap(bm_diamond);       //设置每一小方块为二维数组的一个数据
                iv_game_arr[i][j].setLayoutParams(new RelativeLayout.LayoutParams(src_width,src_width));    //小方块宽高是屏幕的宽高
                iv_game_arr[i][j].setPadding(2,2,2,2);      //方块之间的间距
                iv_game_arr[i][j].setTag(new GameData(i,j,bm_diamond));              //绑定自定义的数据
                iv_game_arr[i][j].setOnClickListener(new View.OnClickListener() {       //点击方块的监听器
                    @Override
                    public void onClick(View v) {
                        boolean flag=isHasByNullImageView((ImageView) v);       //标记点击的方块是否为空
                    /*    Toast.makeText(MainActivity.this,"当前点击的方块是否在空白块的相邻位置"+flag,
                                Toast.LENGTH_SHORT).show();         //测试是否正确*/
                        if(flag){
                            changeDataByImageView((ImageView) v);   //符合条件就可以移动了
                        }
                    }
                });
            }
        }
        /**初始化游戏主界面，并添加小方块*/
        gl_game_main= (GridLayout) findViewById(R.id.gl_picture_main);
        for(int i=0;i<iv_game_arr.length;i++){
            for(int j=0;j<iv_game_arr[0].length;j++){
                gl_game_main.addView(iv_game_arr[i][j]);        //给GridLayout添加小方块

            }
        }
        setNullImageView(iv_game_arr[2][4]);    //设置最后一个方块为空
        randomMove();              //初始化随机打乱方块
        isGameStart=true;       //游戏开始状态
    }

    /**重载一个改变方向的方法，*/
    public void changeDirection(int type){
        changeDirection(type,true);
    }
    /**根据手势方向，获取空方块相应的相邻位置，如果存在方块，则进行数据交换，移动,,isAnim确定是否有动画  */
    public void changeDirection(int type,boolean isAnim){      //根据保存的type确定手势方向
        GameData mNullGameData= (GameData) iv_game_null.getTag();   //(绑定的数据对象),获取的空方块的数据
        int new_x=mNullGameData.x;          //新位置坐标
        int new_y=mNullGameData.y;          //new_x和new_y是二维数组上的坐标，因此控制方向的坐标应为：X为行、Y为列
        if(type==1){        //上
            new_x++;                //二维上，上下移动则是行 的变化

        }  else if(type==2){
            new_x--;

           } else if(type==3){
                new_y++;

             } else if(type==4){
                new_y--;
                }
        /**判断新坐标是否存在*/              //iv_game_arr方块数组的行的长度=方块的宽
        if(new_x>=0&&new_x<iv_game_arr.length&&new_y>=0&&new_y<iv_game_arr[0].length){
            if(isAnim) {
                changeDataByImageView(iv_game_arr[new_x][new_y]);   //设置新空方块的位置
            }  else {
                changeDataByImageView(iv_game_arr[new_x][new_y],isAnim);    //没动画的属性同时传过来
              }
        }
    }

    /**判断游戏结束*/
    public void isGameOver(){
        boolean isOver=true;
        for (int i = 0; i <iv_game_arr.length ; i++) {          //遍历数组，判断 小方块与其绑定的数据是否一致
            for (int j = 0; j <iv_game_arr[0].length ; j++) {
                if (iv_game_arr[i][j] == iv_game_null) {        //只要是为空的数据则直接跳过
                    continue;
                }
                GameData mGameData= (GameData) iv_game_arr[i][j].getTag();  //获取数据
                if(!mGameData.isTrue()){         //当游戏没有结束，则会继续执行这个判断方法,当正确时，isOver保存的是true
                    isOver=false;
                    break;
                }
            }

        }
        if(isOver){
            Toast.makeText(MainActivity.this,"游戏结束，智障",Toast.LENGTH_SHORT).show();

        }

    }
    /**初始化手势，并设置监听器*/
    private void initGesture() {
        gestureDetector=new GestureDetector(this, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                int type=getDirection(e1.getX(),e1.getY(),e2.getX(),e2.getY());         //起始和终值点的X/Y
// 测试               Toast.makeText(MainActivity.this,""+type,Toast.LENGTH_SHORT).show();     //字符串型
                changeDirection(type);
                return false;
            }
        });
    }

    private void setNullImageView(ImageView imageView) {        //设置某个方块为空
        imageView.setImageBitmap(null);
        iv_game_null=imageView;
    }

    /**手势判断   手势的起始点和终点
     * 返回的数据代表方向  向上、下、左、右分别为1 2 3 4 */
    public int getDirection(float start_x,float start_y,float end_x,float end_y){
        boolean isLeftOrRight=(Math.abs(start_x-end_x)>Math.abs(start_y-end_y))?true:false;   //预设去判断是否是左右，左右移动，Y是不变
        if(isLeftOrRight){
            boolean isLeft=(start_x-end_x>0)?true:false;    //通过判断起始x与终点x的差判断左右
            if(isLeft){
                return 3;

            } else{             //向右移动
                return 4;
              }
        }  else{                //上下方向
                boolean isUp=(start_y-end_y>0)?true:false;
                if(isUp){
                    return 1;
                }  else{
                    return 2;
                   }
          }
    }

    /**随机打乱顺序,并且是没有动画的*/
    public void randomMove(){
        for (int i = 0; i < 100; i++) {
            int type= (int) (Math.random()*4)+1;        //随机数1-4
            changeDirection(type,false);          //交换，且无动画
        }
    }
    /**重载方法*/
    public void changeDataByImageView(final ImageView imageView){

        changeDataByImageView(imageView,true);        //调用已经写好的

    }
    /**利用动画结束之后交换两个方块*/
    public void changeDataByImageView(final ImageView imageView,boolean isAnim){
        if(isAnimRun){
            return;
        }

        if(!isAnim){
            GameData mGameData= (GameData) imageView.getTag();      //创建对象，用来保存点击的小方块
            iv_game_null.setImageBitmap(mGameData.bm);      //让空方块的数据变为点击的方块的位图数据
            GameData mNullGameData= (GameData) iv_game_null.getTag();   //创建对象取代之前点击的方块成为空方块
            mNullGameData.bm=mGameData.bm;                      //获取原来点击的方块（现在已经变为空方块）的数据
            mNullGameData.pic_x=mGameData.pic_x;
            mNullGameData.pic_y=mGameData.pic_y;
            setNullImageView(imageView);    //设置当前点击的（原来点击的是实方块，交换后）是空方块（整个方法）
            if(isGameStart) {
                isGameOver();   //游戏开始状态下，若移动后（数据交换后）游戏结束则弹出提示{每一次移动去判断}
            }
            return;
        }
        TranslateAnimation translateAnimation=null;
        if(iv_game_null.getY()>imageView.getY()){
            translateAnimation=new TranslateAnimation(0.1f,0.1f,0.1f,imageView.getWidth()); //点击的方块在空白块的上方，向下移动
                                                                            //起始的点（当前点）离View位置的长度，，结束的点与View的距离
          }else if(iv_game_null.getY()<imageView.getY()){
                translateAnimation=new TranslateAnimation(0.1f,-0.1f,0.1f,-imageView.getWidth());

            }else if(iv_game_null.getX()>imageView.getX()){
                    translateAnimation=new TranslateAnimation(0.1f,imageView.getWidth(),0.1f,0.1f);     //向右移动

              }else if(iv_game_null.getX()<imageView.getX()){
                        translateAnimation=new TranslateAnimation(0.1f,-imageView.getWidth(),0.1f,0.1f);
               }
        translateAnimation.setDuration(70);     //设置动画时长
        translateAnimation.setFillAfter(true);  //动画结束之后是否停留
        /**设置动画结束之后要进行数据交换
         * 通过监听器的方式
         * */
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                isAnimRun=true;         //判断动画是可执行的

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isAnimRun=false;
                imageView.clearAnimation();
                GameData mGameData= (GameData) imageView.getTag();      //创建对象，用来保存点击的小方块
                iv_game_null.setImageBitmap(mGameData.bm);      //让空方块的数据变为点击的方块的位图数据
                GameData mNullGameData= (GameData) iv_game_null.getTag();   //创建对象取代之前点击的方块成为空方块
                mNullGameData.bm=mGameData.bm;                      //获取原来点击的方块（现在已经变为空方块）的数据
                mNullGameData.pic_x=mGameData.pic_x;
                mNullGameData.pic_y=mGameData.pic_y;
                setNullImageView(imageView);    //设置当前点击的（原来点击的是实方块，交换后）是空方块（整个方法）
                if(isGameStart) {
                    isGameOver();   //游戏开始状态下，若移动后游戏结束则弹出提示{首次打乱后去判断
                }

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        imageView.startAnimation(translateAnimation);   //执行动画


    }
    /**分别获取当前空方块的位置和点击方块的位置，通过x、y都差1的方式 */
    public boolean isHasByNullImageView(ImageView imageView){   //判断当前点击的方块是否在空方块的相邻位置

        GameData gd_null=(GameData)iv_game_null.getTag();       //空的方块
        GameData mGameData= (GameData) imageView.getTag();      //当前点击的方块


        if(gd_null.x==mGameData.x&&(gd_null.y)-1==mGameData.y){                               //当前点击的方块在空方块的上边

            return true;
        } else if(gd_null.x==mGameData.x&&(gd_null.y)+1==mGameData.y){
            return true;

          } else if(gd_null.y==mGameData.y&&(gd_null.x)-1==mGameData.x){                      //当前点击的方块在空方块的左边
            return true;

           } else if(gd_null.y==mGameData.y&&(gd_null.x)+1==mGameData.x){
            return true;

             }
        return false;
    }
    class GameData{             //每一个小方块要绑定的数据

        public int x=0;         //每个小方块的实际位置
        public int y=0;
        public Bitmap bm;       //小方块的图片
        public int pic_x=0;     //小方块图片的位置
        public int pic_y=0;

        public GameData(int x, int y, Bitmap bm) {
            this.x = x;
            this.y = y;
            this.bm = bm;
            this.pic_x = x;     //初始时两个位置是一样的
            this.pic_y = y;
        }
        /**判断每个小方块是否正确*/
        public boolean isTrue() {
            if(x==pic_x&&y==pic_y){     //坐标想相等
                return true;
            }
            return false;
        }
    }
}
