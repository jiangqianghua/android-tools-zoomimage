package com.jqh.imageview;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;

import java.util.logging.Logger;

/**
 * Created by jiangqianghua on 2016/6/18.
 */
public class ZoomImageView extends AppCompatImageView implements ViewTreeObserver.OnGlobalLayoutListener,
        ScaleGestureDetector.OnScaleGestureListener,View.OnTouchListener{
    /////// 缩放
    //  确保加载图片只是执行一次
    private boolean mOnce = false ;
    //  初始化缩小的值
    private float mInitScale ;
    //点击放大值到最大值
    private float mMidScale ;
    //  放大的最大值
    private float mMaxScale ;

    private Matrix mScaleMatrix ;
    /**
     * 捕获多点触摸缩放比例
     */
    private ScaleGestureDetector mScaleGestureDetector ;

    ///////////自由移动
    /**
     * 记录上一次多点触控的数量
     */
    private int mLastPointerCount ;
    //  记录最后一次的中心点位置
    private float mLastX ;
    private float mLastY ;
    //表示滑动的时候，手的移动要大于这个距离才开始移动控件。如果小于这个距离就不触发移动控件
    private int mTouchSlop ;
    private boolean isCanDrag = false ;

    private boolean isCheckLeftAndRight ;
    private boolean isCheckTopandBottom ;


    ///// 双击缩放
    private GestureDetector mGestureDetector ;
    // 判断是否已经在自动缩放
    private boolean isAutoScale = false;
    public ZoomImageView(Context context) {
        super(context);
        initData(context);
    }

    public ZoomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initData(context);
    }

    public ZoomImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // init
        initData(context);
    }

    private void initData(Context context)
    {
        Log.d("imageview111","imageview111： initData");
        mScaleMatrix = new Matrix();
        setScaleType(ScaleType.MATRIX);
        mScaleGestureDetector = new ScaleGestureDetector(context , this);
        setOnTouchListener(this);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop() ;
        // 注册双击事件
        mGestureDetector = new GestureDetector(context,new GestureDetector.SimpleOnGestureListener()
        {
            @Override
            public boolean onDoubleTap(MotionEvent e) {

                if(isAutoScale)  return true ;
                float x = e.getX() ;
                float y = e.getY() ;
                if(getScale() < mMidScale)
                {
//                    mScaleMatrix.postScale(mMidScale/getScale() , mMidScale/getScale(),x, y);
//                    setImageMatrix(mScaleMatrix);

                    postDelayed(new AutoScaleRunnable(mMidScale,x,y),16);
                    isAutoScale = true ;
                }
                else
                {
//                    mScaleMatrix.postScale(mInitScale/getScale() , mInitScale/getScale(),x, y);
//                    setImageMatrix(mScaleMatrix);
                    postDelayed(new AutoScaleRunnable(mInitScale,x,y),16);
                    isAutoScale  = true ;
                }
                return true;
            }
        });
    }

    /**
     * 确保双击使得图片缓慢变化
     */
    private class AutoScaleRunnable implements Runnable
    {
        // 缩放的目标值
        private float mTargetScale ;
        // 缩放的中心点
        private float x ;
        private float y ;
        // 缩放梯度
        private final float BIGGER = 1.07f ;
        private final float SMALL = 0.93f ;
        private float tmpScale ;
        public AutoScaleRunnable(float mTargetScale, float x, float y) {
            this.mTargetScale = mTargetScale;
            this.x = x;
            this.y = y;
            if(getScale() < mTargetScale)
            {
                tmpScale = BIGGER ;
            }

            if(getScale() > mTargetScale)
                tmpScale = SMALL ;
        }

        @Override
        public void run() {
            mScaleMatrix.postScale(tmpScale, tmpScale,x, y);
            checkBoderAndCenterWhenScale();
            setImageMatrix(mScaleMatrix);
            float currentScale = getScale() ;
            if((tmpScale > 1.0f && currentScale < mTargetScale)||
                    (tmpScale < 1.0f && currentScale > mTargetScale))

            {
                postDelayed(this,16) ; //  每16毫秒执行一次
            }
            else  // 设置我们的目标值
            {
                float  scale = mTargetScale/currentScale ;
                mScaleMatrix.postScale(scale, scale, x, y);
                checkBoderAndCenterWhenScale();
                setImageMatrix(mScaleMatrix);
                isAutoScale = false ;
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
    }

    /**
     * 完成imageview加载完成图片
     */
    @Override
    public void onGlobalLayout() {

        Log.d("imageview111", "imageview111： onGlobalLayout");
        if (!mOnce)
        {
            //  控件宽高
            int width = getWidth();
            int height = getHeight() ;

            //  获取图片的宽高
            Drawable d = getDrawable() ;
            if(d == null)
                return ;
            int dw = d.getIntrinsicWidth() ;
            int dh = d.getIntrinsicHeight() ;
            Log.d("imageview111", "imageview111： dw="+dw + " dh="+dh);
            Log.d("imageview111", "imageview111： w="+width + " h="+height);
            float scale = 1.0f ;
            if(dw > width && dh < height)
            {
                scale = width * 1.0f/dw ;
            }

            if(dh > height && dw < width)
            {
                scale = height*1.0f/dh ;
            }

            if(dw > width && dh > height)
            {
                scale = Math.min(width*1.0f/dw , height*1.0f/dw);
            }

            if(dw < width && dh < height)
            {
                scale = Math.min(width*1.0f/dw , height*1.0f/dw);
            }
            //  初始化缩放比例
            mInitScale = scale ;
            mMaxScale = mInitScale * 4 ;
            mMidScale = mInitScale*2 ;

            // 将图片放入中心位置
            int dx = width/2 - dw/2 ;
            int dy = height/2 - dh/2 ;
            Log.d("imageview111", "imageview111： dx="+dx + " dy="+dy);
            mScaleMatrix.postTranslate(dx, dy);
            mScaleMatrix.postScale(mInitScale,mInitScale,width/2,height/2);
            // 移动图片位置
            setImageMatrix(mScaleMatrix);
            mOnce = true ;
        }

  }

    /**
     * 获取当前缩放比例
     * @return
     */
    private float getScale()
    {
        float[] values = new float[9];
        mScaleMatrix.getValues(values);
        return values[Matrix.MSCALE_X];
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {

        float scale = getScale() ;
        // 获取缩放比例
        float scaleFactor = detector.getScaleFactor() ;
        //scaleFactor  表示缩小还是放大，需要看与1的关系
        if(getDrawable() == null)
                return true  ;
        // 缩放范围控制
        if((scale < mMaxScale && scaleFactor > 1.0f) || (scale > mInitScale && scaleFactor < 1.0f))
        {
            if(scale*scaleFactor < mInitScale)
            {
                scaleFactor = mInitScale/scale ;
            }

            if(scale*scaleFactor > mMaxScale)
            {
                scale = mMaxScale/scale ;
            }
            //   以中心点位置缩放
           // mScaleMatrix.postScale(scaleFactor,scaleFactor,getWidth()/2,getHeight()/2);
            //  以指定触摸点位置缩放
            mScaleMatrix.postScale(scaleFactor,scaleFactor,detector.getFocusX(),detector.getFocusY());
            checkBoderAndCenterWhenScale();
            setImageMatrix(mScaleMatrix);
        }
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        //  双击的时候不产生移动的操作
        if(mGestureDetector.onTouchEvent(event))
            return true ;
        mScaleGestureDetector.onTouchEvent(event);

        //  设置自由移动
        float  x = 0  ;
        float y = 0 ;
        // 获取多点触控的数量
        int pointerCount = event.getPointerCount() ;
        for (int i = 0 ; i < pointerCount ; i++)
        {
            x += event.getX(i);
            y += event.getY(i);
        }

        x /=pointerCount ;
        y /=pointerCount ;

        if(mLastPointerCount != pointerCount)
        {
            //  记录中心点位置
            isCanDrag = false ;
            mLastX = x ;
            mLastY = y ;
        }
        mLastPointerCount = pointerCount ;
        RectF rectF1 = getMatrixRectF();

        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                //  判断是否拦截外面的事件
                if(rectF1.width() > getWidth() + 0.01 || rectF1.height() > getHeight() + 0.01)
                {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                //  判断是否拦截外面的事件
                if(rectF1.width() > getWidth()+0.01 || rectF1.height() > getHeight()+0.01)
                {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                float dx = x - mLastX ;
                float dy = y - mLastY ;
                Log.d("imageview111", "imageview111：move dx="+dx + " dy="+dy + " mTouchSlop="+mTouchSlop);
                if(!isCanDrag)
                {
                    isCanDrag = isMoveAction(dx ,dy) ;
                }

                if(isCanDrag)
                {
                    // 完成图片移动
                    RectF rectF = getMatrixRectF();
                    if(getDrawable() != null)
                    {
                        isCheckLeftAndRight = isCheckTopandBottom = true ;
                        //如果图片小于控件宽度，不允许横向移动
                        if(rectF.width() < getWidth())
                        {
                            isCheckLeftAndRight = false ;
                            dx = 0 ;
                        }
                        //如果图片小于控件高度，不允许纵向移动
                        if(rectF.height() < getHeight())
                        {
                            isCheckTopandBottom = false ;
                            dy = 0 ;
                        }

                        mScaleMatrix.postTranslate(dx , dy);
                        checkBorderWhenTranslate();
                        setImageMatrix(mScaleMatrix);
                    }
                }
                mLastX = x ;
                mLastY = y ;
                break ;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mLastPointerCount = 0 ;

                break;
            default:
                break;
        }
        return true;
    }

    /**
     * 获取图片缩放以后的宽高，包括l,r,t ,b
     * @return
     */
    private RectF getMatrixRectF()
    {
        Matrix matrix = mScaleMatrix ;
        RectF rectF = new RectF();
        Drawable d = getDrawable() ;
        if(d != null)
        {
            rectF.set(0,0,d.getIntrinsicWidth(),d.getIntrinsicHeight());
            matrix.mapRect(rectF);
        }
        return rectF ;
    }
    /**
     * 在缩放的时候进行边界和位置控制
     */
    private void checkBoderAndCenterWhenScale()
    {
        RectF rect = getMatrixRectF();
        float deltax = 0 ;
        float deltay = 0 ;

        int width = getWidth() ;
        int height = getHeight() ;

        if(rect.width() >= width)
        {
            if(rect.left > 0)
            {
                deltax = -rect.left ;
            }

            if(rect.right < width)
            {
                deltax = width - rect.right ;
            }
        }

        if (rect.height() >= height)
        {
            if(rect.top > 0)
            {
                deltax = -rect.top ;
            }
            if(rect.bottom < height)
            {
                deltay = height - rect.bottom ;
            }
        }
        //  如果图片宽高小于空间宽高
        if(rect.width() < width)
        {
            deltax = width/2f - rect.right + rect.width()/2f ;
        }

        if(rect.height() < height)
        {
            deltay = height/2f - rect.bottom + rect.height()/2f ;
        }

        mScaleMatrix.postTranslate(deltax , deltay);
    }

    /**
     * 在移动的时候进行边界和位置控制
     */
    private void checkBorderWhenTranslate()
    {
        RectF rectF = getMatrixRectF();
        float deltax = 0 ;
        float deltay = 0 ;

        int width = getWidth() ;
        int height = getHeight() ;

        if(rectF.top > 0 && isCheckTopandBottom)
        {
            deltay = -rectF.top ;
        }

        if(rectF.bottom < height && isCheckTopandBottom)
        {
            deltay = height - rectF.bottom ;
        }

        if(rectF.left > 0 && isCheckLeftAndRight)
        {
            deltax = -rectF.left ;
        }

        if(rectF.right < width && isCheckLeftAndRight)
        {
            deltax = width - rectF.right ;
        }

        //  如果图片宽高小于空间宽高
        if(rectF.width() < width)
        {
            deltax = width/2f - rectF.right + rectF.width()/2f ;
        }

        if(rectF.height() < height)
        {
            deltay = height/2f - rectF.bottom + rectF.height()/2f ;
        }

        mScaleMatrix.postTranslate(deltax , deltay);
    }

    /**
     * 判断是否是move
     * @param dx
     * @param dy
     * @return
     */
    private boolean isMoveAction(float dx , float dy)
    {
        return Math.sqrt(dx*dx + dy*dy) > mTouchSlop ;
    }
}
