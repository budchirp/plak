use adw::{prelude::*, Application, ApplicationWindow, OverlaySplitView, Toast, ToastOverlay};
use async_channel::{Receiver, Sender};
use gtk::{
    gdk::Display, style_context_add_provider_for_display, CssProvider, Stack,
    STYLE_PROVIDER_PRIORITY_APPLICATION,
};
use sidebar_page::SidebarPage;
use views::instance_view::InstanceView;

use crate::{config::instance_config::InstanceConfigManager, constants};

use content_page::ContentPage;

pub mod content_page;
mod dialogs;
pub mod sidebar_page;
mod views;

#[derive(Clone)]
pub struct MainWindow {
    pub window: ApplicationWindow,

    toast_overlay: ToastOverlay,
    stack_content: Stack,

    sender: Sender<String>,
}

impl MainWindow {
    pub fn new(app: &Application) -> Self {
        let (sender, receiver) = async_channel::unbounded();

        let mut main_window = Self {
            window: ApplicationWindow::builder()
                .application(app)
                .title(constants::APP_NAME)
                .default_width(800)
                .default_height(600)
                .build(),

            toast_overlay: ToastOverlay::new(),
            stack_content: Stack::new(),

            sender,
        };

        main_window.signal_receiver(receiver);
        main_window
    }

    fn signal_receiver(&mut self, receiver: Receiver<String>) {
        let mut window = self.clone();
        gtk::glib::spawn_future_local(async move {
            while let Ok(signal_name) = receiver.recv().await {
                match signal_name.as_str() {
                    "refresh" => window.set_content(),
                    _ => println!("unknown signal"),
                }
            }
        });
    }

    pub fn build_ui(&mut self) {
        self.set_css_provider();

        self.set_content();
        self.window.set_content(Some(&self.toast_overlay));
    }

    pub fn set_content(&mut self) {
        self.set_stack_content();

        let split_view = OverlaySplitView::builder()
            .enable_show_gesture(true)
            .enable_hide_gesture(true)
            .sidebar(&SidebarPage::new(
                &self.window,
                &self.stack_content,
                &self.toast_overlay,
                &self.sender,
            ))
            .content(&ContentPage::new(&self.stack_content))
            .build();

        self.toast_overlay.set_child(Some(&split_view));
    }

    fn set_stack_content(&mut self) {
        self.stack_content = Stack::new();
        self.stack_content.set_halign(gtk::Align::Start);
        self.stack_content.set_valign(gtk::Align::Start);

        self.get_instances();
    }

    fn get_instances(&self) {
        let instances = InstanceConfigManager::get_all();
        match instances {
            Err(_) => {
                let toast = Toast::new("Failed to get instances");
                self.toast_overlay.add_toast(toast);
            }
            Ok(instances) => {
                for instance_config_struct in instances {
                    self.stack_content.add_titled(
                        &InstanceView::new(
                            self.toast_overlay.clone(),
                            instance_config_struct.clone(),
                            self.sender.clone(),
                        ),
                        Some(&instance_config_struct.slug),
                        &instance_config_struct.name,
                    );
                }
            }
        }
    }

    fn set_css_provider(&self) {
        let provider = CssProvider::new();
        provider.load_from_string(include_str!("../ui.css"));

        style_context_add_provider_for_display(
            &Display::default().expect(""),
            &provider,
            STYLE_PROVIDER_PRIORITY_APPLICATION,
        );
    }
}
