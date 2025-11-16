import UIKit

public class ColorHelper
{
    // MARK: - Constants

    private let SHADOW_ADJ = -333
    private let HIGHLIGHT_ADJ = 500
    private let WATERMARK_ADJ = -50
    private let RANGE = 240
    private let HLS_MAX = 240
    private let RGB_MAX = 255
    private let UNDEFINED: Int

    // MARK: - Internal Variables

    private var m_Hue: Int = 0
    private var m_Saturation: Int = 0
    private var m_Luminosity: Int = 0

    // MARK: - Properties

    public var Luminosity: Int
    {
        return m_Luminosity
    }

    // MARK: - Initializer

    public init(color: UIColor)
    {
        self.UNDEFINED = HLS_MAX * 2 / 3

        var rf: CGFloat = 0
        var gf: CGFloat = 0
        var bf: CGFloat = 0
        var af: CGFloat = 0
        color.getRed(&rf, green: &gf, blue: &bf, alpha: &af)

        let r = Int(rf * 255)
        let g = Int(gf * 255)
        let b = Int(bf * 255)

        let maxVal = max(r, g, b)
        let minVal = min(r, g, b)
        let sum = maxVal + minVal
        m_Luminosity = ((sum * HLS_MAX + RGB_MAX) / (2 * RGB_MAX))

        let dif = maxVal - minVal
        if dif == 0
        {
            m_Saturation = 0
            m_Hue = UNDEFINED
        }
        else
        {
            if m_Luminosity <= (HLS_MAX / 2)
            {
                m_Saturation = (dif * HLS_MAX + (sum / 2)) / sum
            }
            else
            {
                m_Saturation = ((dif * HLS_MAX) + ((2 * RGB_MAX - sum) / 2)) / (2 * RGB_MAX - sum)
            }

            let rDelta = ((maxVal - r) * (HLS_MAX / 6) + (dif / 2)) / dif
            let gDelta = ((maxVal - g) * (HLS_MAX / 6) + (dif / 2)) / dif
            let bDelta = ((maxVal - b) * (HLS_MAX / 6) + (dif / 2)) / dif

            if r == maxVal
            {
                m_Hue = bDelta - gDelta
            }
            else if g == maxVal
            {
                m_Hue = (HLS_MAX / 3) + rDelta - bDelta
            }
            else
            {
                m_Hue = ((2 * HLS_MAX) / 3) + gDelta - rDelta
            }

            if m_Hue < 0 { m_Hue += HLS_MAX }
            if m_Hue > HLS_MAX { m_Hue -= HLS_MAX }
        }
    }

    // MARK: - Public API

    public func Darker(_ percDarker: CGFloat) -> UIColor
    {
        let oneLum = 0
        let zeroLum = NewLuma(n: SHADOW_ADJ, scale: true)
        return ColorFromHLS(hue: m_Hue,
                            luminosity: zeroLum - Int(CGFloat(zeroLum - oneLum) * percDarker),
                            saturation: m_Saturation)
    }

    public func Lighter(_ percLighter: CGFloat) -> UIColor
    {
        let zeroLum = m_Luminosity
        let oneLum = NewLuma(n: HIGHLIGHT_ADJ, scale: true)
        return ColorFromHLS(hue: m_Hue,
                            luminosity: zeroLum + Int(CGFloat(oneLum - zeroLum) * percLighter),
                            saturation: m_Saturation)
    }

    // MARK: - Helper Functions

    private func NewLuma(n: Int, scale: Bool) -> Int
    {
        return NewLuma(luminosity: m_Luminosity, n: n, scale: scale)
    }

    private func NewLuma(luminosity: Int, n: Int, scale: Bool) -> Int
    {
        if n == 0 { return luminosity }

        if scale
        {
            if n > 0
            {
                return (luminosity * (1000 - n) + (RANGE + 1) * n) / 1000
            }
            else
            {
                return (luminosity * (n + 1000)) / 1000
            }
        }

        var newLum = luminosity + (n * RANGE / 1000)
        if newLum < 0 { newLum = 0 }
        if newLum > HLS_MAX { newLum = HLS_MAX }

        return newLum
    }

    private func ColorFromHLS(hue: Int, luminosity: Int, saturation: Int) -> UIColor
    {
        var r: Int = 0
        var g: Int = 0
        var b: Int = 0

        if saturation == 0
        {
            r = (luminosity * RGB_MAX) / HLS_MAX
            g = r
            b = r
        }
        else
        {
            let magic2: Int
            if luminosity <= (HLS_MAX / 2)
            {
                magic2 = (luminosity * (HLS_MAX + saturation) + (HLS_MAX / 2)) / HLS_MAX
            }
            else
            {
                magic2 = luminosity + saturation - ((luminosity * saturation + (HLS_MAX / 2)) / HLS_MAX)
            }

            let magic1 = 2 * luminosity - magic2

            r = ((HueToRGB(n1: magic1, n2: magic2, hue: hue + HLS_MAX / 3) * RGB_MAX + (HLS_MAX / 2))) / HLS_MAX
            g = ((HueToRGB(n1: magic1, n2: magic2, hue: hue) * RGB_MAX + (HLS_MAX / 2))) / HLS_MAX
            b = ((HueToRGB(n1: magic1, n2: magic2, hue: hue - HLS_MAX / 3) * RGB_MAX + (HLS_MAX / 2))) / HLS_MAX
        }

        return UIColor(red: CGFloat(r) / 255.0,
                       green: CGFloat(g) / 255.0,
                       blue: CGFloat(b) / 255.0,
                       alpha: 1.0)
    }

    private func HueToRGB(n1: Int, n2: Int, hue: Int) -> Int
    {
        var hueVal = hue
        if hueVal < 0 { hueVal += HLS_MAX }
        if hueVal > HLS_MAX { hueVal -= HLS_MAX }

        if hueVal < (HLS_MAX / 6)
        {
            return n1 + ((n2 - n1) * hueVal + (HLS_MAX / 12)) / (HLS_MAX / 6)
        }
        if hueVal < (HLS_MAX / 2)
        {
            return n2
        }
        if hueVal < ((HLS_MAX * 2) / 3)
        {
            return n1 + ((n2 - n1) * ((HLS_MAX * 2 / 3) - hueVal) + (HLS_MAX / 12)) / (HLS_MAX / 6)
        }
        return n1
    }
}
