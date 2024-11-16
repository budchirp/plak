use adw::{
    prelude::*, ApplicationWindow, HeaderBar, NavigationPage, ToastOverlay, ToolbarView,
    WindowTitle,
};
use async_channel::Sender;
use gtk::{Stack, StackSidebar, ToggleButton};

use crate::constants;

use super::dialogs::add_instance_dialog::AddInstanceDialog;

pub struct SidebarPage {}

impl SidebarPage {
    pub fn new(
        window: &ApplicationWindow,
        stack_content: &Stack,
        toast_overlay: &ToastOverlay,
        sender: &Sender<String>,
    ) -> NavigationPage {
        let sidebar_content = StackSidebar::builder()
            .vexpand(true)
            .stack(&stack_content)
            .build();

        let sidebar_header_bar_title = WindowTitle::new(constants::APP_NAME, "");
        let sidebar_header_add_button = ToggleButton::builder()
            .icon_name("list-add-symbolic")
            .build();
        let sidebar_header_refresh_button = ToggleButton::builder()
            .icon_name("view-refresh-symbolic")
            .build();

        let window_clone = window.clone();
        let toast_overlay_clone = toast_overlay.clone();
        let sender_clone = sender.clone();
        sidebar_header_add_button.connect_clicked(move |_| {
            gtk::glib::MainContext::default().spawn_local(AddInstanceDialog::new(
                window_clone.clone(),
                toast_overlay_clone.clone(),
                sender_clone.clone(),
            ));
        });

        let sender_clone = sender.clone();
        sidebar_header_refresh_button.connect_clicked(move |_| {
            let sender_clone = sender_clone.clone();
            gtk::gio::spawn_blocking(move || {
                sender_clone
                    .send_blocking("refresh".to_string())
                    .expect("Failed to send message");
            });
        });

        let sidebar_header_bar = HeaderBar::builder()
            .title_widget(&sidebar_header_bar_title)
            .css_classes(["devel"])
            .build();
        sidebar_header_bar.pack_start(&sidebar_header_add_button);
        sidebar_header_bar.pack_end(&sidebar_header_refresh_button);

        let sidebar_toolbar = ToolbarView::builder().content(&sidebar_content).build();
        sidebar_toolbar.add_top_bar(&sidebar_header_bar);

        NavigationPage::new(&sidebar_toolbar, "Sidebar")
    }
}
