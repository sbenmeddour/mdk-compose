import UIKit
import SwiftUI
import KotlinApp

struct ComposeView : UIViewControllerRepresentable {
    
    func makeUIViewController(context: Context) -> some UIViewController {
        return MainViewControllerKt.MainViewController()
    }
    
    func updateUIViewController(_ uiViewController: UIViewControllerType, context: Context) {
        
    }
}

struct ContentView: View {
    var body: some View {
        ComposeView()
    }
}



