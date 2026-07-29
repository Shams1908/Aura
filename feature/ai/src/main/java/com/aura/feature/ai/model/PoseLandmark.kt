package com.aura.feature.ai.model

data class Point3D(val x: Float, val y: Float, val z: Float, val likelihood: Float)

data class PoseLandmarks(
    val leftShoulder: Point3D?,
    val rightShoulder: Point3D?,
    val leftHip: Point3D?,
    val rightHip: Point3D?,
    val leftElbow: Point3D?,
    val rightElbow: Point3D?,
    val leftWrist: Point3D?,
    val rightWrist: Point3D?,
    val leftKnee: Point3D?,
    val rightKnee: Point3D?,
    val leftAnkle: Point3D?,
    val rightAnkle: Point3D?
)
