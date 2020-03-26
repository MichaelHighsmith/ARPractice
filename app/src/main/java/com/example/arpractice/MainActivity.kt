package com.example.arpractice

import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.animation.ModelAnimator
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.BaseArFragment
import com.google.ar.sceneform.ux.TransformableNode

class MainActivity : AppCompatActivity() {

    private var arFragment: ArFragment? = null
    private var anchorNode: AnchorNode? = null
    private var animator: ModelAnimator? = null
    private var nextAnimation = 0
    private var btn_anim: FloatingActionButton? = null
    private var cube: ModelRenderable? = null
    private var transformableNode: TransformableNode? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        arFragment =
            (supportFragmentManager.findFragmentById(R.id.sceneform_fragment) as ArFragment)

        //Tap on plane event
        arFragment?.setOnTapArPlaneListener(object : BaseArFragment.OnTapArPlaneListener {

            override fun onTapPlane(hitResult: HitResult?,
                                    plane: Plane?,
                                    motionEvent: MotionEvent?) {

                if(cube == null) {
                    return
                }
                val anchor = hitResult?.createAnchor()
                if (anchorNode == null) {
                    anchorNode = AnchorNode(anchor)
                    anchorNode?.setParent(arFragment?.arSceneView?.scene)

                    transformableNode = TransformableNode(arFragment?.transformationSystem)
                    transformableNode?.scaleController?.minScale = 0.09f
                    transformableNode?.scaleController?.maxScale = 0.1f
                    transformableNode?.setParent(anchorNode)
                    transformableNode?.renderable = cube
                }
            }
        })

        arFragment?.arSceneView?.scene?.addOnUpdateListener(object : Scene.OnUpdateListener {
            override fun onUpdate(frameTime: FrameTime?) {
                if (anchorNode == null) {
                    btn_anim?.let {
                        if (it.isEnabled) {
                            it.backgroundTintList = ColorStateList.valueOf(android.graphics.Color.GRAY)
                            it.isEnabled = false
                        }
                    }

                } else {
                    btn_anim?.let {
                        if (!it.isEnabled) {
                            it.backgroundTintList = ContextCompat.getColorStateList(this@MainActivity, R.color.colorPrimary)
                            it.isEnabled = true
                        }
                    }
                }
            }
        })

        btn_anim = findViewById(R.id.btn_anim)
        btn_anim?.isEnabled = false
        btn_anim?.setOnClickListener {
            animator?.let {
                if (it.isRunning) {
                    val data = cube?.getAnimationData(nextAnimation)
                    nextAnimation = (nextAnimation + 1) % (cube?.animationDataCount ?: 0)
                    animator = ModelAnimator(data, cube)
                    animator?.start()
                }
            }

            if (animator == null) {
                try {
                    val data = cube?.getAnimationData(nextAnimation)
                    nextAnimation = (nextAnimation + 1) % (cube?.animationDataCount ?: 0)
                    animator = ModelAnimator(data, cube)
                    animator?.start()
                } catch (e: IndexOutOfBoundsException) {
                    print("index animation error")
                }

            }
        }

        setupModel()
    }

    private fun setupModel() {
        ModelRenderable.builder()
            .setSource(this, R.raw.cube)
            .build()
            .thenAccept{renderable -> cube = renderable}
            .exceptionally { throwable ->
                return@exceptionally null
            }
    }
}
