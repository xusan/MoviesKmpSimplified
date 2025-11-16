import UIKit

public extension UIView
{
    // MARK: - Fade

    func Fade(_ isIn: Bool, duration: TimeInterval = 0.3, onFinished: (() -> Void)? = nil)
    {
        let minAlpha: CGFloat = 0.0
        let maxAlpha: CGFloat = 1.0

        self.alpha = isIn ? minAlpha : maxAlpha
        self.transform = .identity

        UIView.animate(withDuration: duration,
                       delay: 0,
                       options: .curveEaseInOut,
                       animations:
                       {
                           self.alpha = isIn ? maxAlpha : minAlpha
                       },
                       completion: { _ in onFinished?() })
    }

    // MARK: - Slide Vertically

    func SlideVerticaly(_ isIn: Bool, fromTop: Bool, duration: TimeInterval = 0.3, onFinished: (() -> Void)? = nil)
    {
        let minAlpha: CGFloat = 0.0
        let maxAlpha: CGFloat = 1.0
        let minTransform = CGAffineTransform(translationX: 0, y: (fromTop ? -1 : 1) * self.bounds.height)
        let maxTransform = CGAffineTransform.identity

        self.alpha = isIn ? minAlpha : maxAlpha
        self.transform = isIn ? minTransform : maxTransform

        UIView.animate(withDuration: duration,
                       delay: 0,
                       options: .curveEaseInOut,
                       animations:
                       {
                           self.alpha = isIn ? maxAlpha : minAlpha
                           self.transform = isIn ? maxTransform : minTransform
                       },
                       completion: { _ in onFinished?() })
    }

    // MARK: - Slide Horizontally

    func SlideHorizontaly(_ isIn: Bool, fromLeft: Bool, duration: TimeInterval = 0.3, onFinished: (() -> Void)? = nil)
    {
        let minAlpha: CGFloat = 0.0
        let maxAlpha: CGFloat = 1.0
        let minTransform = CGAffineTransform(translationX: (fromLeft ? -1 : 1) * self.bounds.width, y: 0)
        let maxTransform = CGAffineTransform.identity

        self.alpha = isIn ? minAlpha : maxAlpha
        self.transform = isIn ? minTransform : maxTransform

        UIView.animate(withDuration: duration,
                       delay: 0,
                       options: .curveEaseInOut,
                       animations:
                       {
                           self.alpha = isIn ? maxAlpha : minAlpha
                           self.transform = isIn ? maxTransform : minTransform
                       },
                       completion: { _ in onFinished?() })
    }

    // MARK: - Scale

    func Scale(_ isIn: Bool, duration: TimeInterval = 0.3, onFinished: (() -> Void)? = nil)
    {
        let minAlpha: CGFloat = 0.0
        let maxAlpha: CGFloat = 1.0
        let minTransform = CGAffineTransform(scaleX: 0.1, y: 0.1)
        let maxTransform = CGAffineTransform(scaleX: 1, y: 1)

        self.alpha = isIn ? minAlpha : maxAlpha
        self.transform = isIn ? minTransform : maxTransform

        UIView.animate(withDuration: duration,
                       delay: 0,
                       options: .curveEaseInOut,
                       animations:
                       {
                           self.alpha = isIn ? maxAlpha : minAlpha
                           self.transform = isIn ? maxTransform : minTransform
                       },
                       completion: { _ in onFinished?() })
    }

    // MARK: - Rotate

    func Rotate(_ isIn: Bool, fromLeft: Bool = true, duration: TimeInterval = 0.3, onFinished: (() -> Void)? = nil)
    {
        let minAlpha: CGFloat = 0.0
        let maxAlpha: CGFloat = 1.0
        let degrees: CGFloat = (fromLeft ? -1 : 1) * 720
        let radians = degrees * (.pi / 180)
        let minTransform = CGAffineTransform(rotationAngle: radians)
        let maxTransform = CGAffineTransform(rotationAngle: 0)

        self.alpha = isIn ? minAlpha : maxAlpha
        self.transform = isIn ? minTransform : maxTransform

        UIView.animate(withDuration: duration,
                       delay: 0,
                       options: .curveEaseInOut,
                       animations:
                       {
                           self.alpha = isIn ? maxAlpha : minAlpha
                           self.transform = isIn ? maxTransform : minTransform
                       },
                       completion: { _ in onFinished?() })
    }

    // MARK: - Flip Vertically

    func FlipVerticaly(_ isIn: Bool, duration: TimeInterval = 0.3, onFinished: (() -> Void)? = nil)
    {
        let m34: CGFloat = -0.001
        let minAlpha: CGFloat = 0.0
        let maxAlpha: CGFloat = 1.0

        var minTransform = CATransform3DIdentity
        minTransform.m34 = m34
        minTransform = CATransform3DRotate(minTransform, CGFloat((isIn ? 1 : -1)) * .pi * 0.5, 1, 0, 0)

        var maxTransform = CATransform3DIdentity
        maxTransform.m34 = m34

        self.alpha = isIn ? minAlpha : maxAlpha
        self.layer.transform = isIn ? minTransform : maxTransform

        UIView.animate(withDuration: duration,
                       delay: 0,
                       options: .curveEaseInOut,
                       animations:
                       {
                           self.layer.anchorPoint = CGPoint(x: 0.5, y: 0.5)
                           self.layer.transform = isIn ? maxTransform : minTransform
                           self.alpha = isIn ? maxAlpha : minAlpha
                       },
                       completion: { _ in onFinished?() })
    }

    // MARK: - Flip Horizontally

    func FlipHorizontaly(_ isIn: Bool, duration: TimeInterval = 0.3, onFinished: (() -> Void)? = nil)
    {
        let m34: CGFloat = -0.001
        let minAlpha: CGFloat = 0.0
        let maxAlpha: CGFloat = 1.0

        var minTransform = CATransform3DIdentity
        minTransform.m34 = m34
        minTransform = CATransform3DRotate(minTransform, CGFloat((isIn ? 1 : -1)) * .pi * 0.5, 0, 1, 0)

        var maxTransform = CATransform3DIdentity
        maxTransform.m34 = m34

        self.alpha = isIn ? minAlpha : maxAlpha
        self.layer.transform = isIn ? minTransform : maxTransform

        UIView.animate(withDuration: duration,
                       delay: 0,
                       options: .curveEaseInOut,
                       animations:
                       {
                           self.layer.anchorPoint = CGPoint(x: 0.5, y: 0.5)
                           self.layer.transform = isIn ? maxTransform : minTransform
                           self.alpha = isIn ? maxAlpha : minAlpha
                       },
                       completion: { _ in onFinished?() })
    }
    
    func findFirstResponder() -> UIView?
    {
        if self.isFirstResponder { return self }
        for subview in subviews {
            if let responder = subview.findFirstResponder() {
                return responder
            }
        }
        return nil
    }
}
