package seravifer.apipoliformat

/**
 * Created by David Olmos on 25/02/2017.
 */
fun Double.round(d: Int): Double = Math.round(this*Math.pow(10.0, d.toDouble()))/Math.pow(10.0, d.toDouble())
