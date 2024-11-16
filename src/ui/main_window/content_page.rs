use adw::{HeaderBar, NavigationPage, ToolbarView};
use gtk::{gio::Menu, MenuButton, Stack};

pub struct ContentPage {}

impl ContentPage {
    pub fn new(stack_content: &Stack) -> NavigationPage {
        let menu = Menu::new();
        menu.append(Some("About"), Some("app.about"));
        menu.append(Some("Quit"), Some("app.quit"));

        let stack_toolbar = ToolbarView::builder().content(stack_content).build();

        let stack_header_bar = HeaderBar::builder()
            .show_title(false)
            .css_classes(["devel"])
            .build();
        stack_toolbar.add_top_bar(&stack_header_bar);

        let menu_button = MenuButton::builder()
            .menu_model(&menu)
            .icon_name("open-menu-symbolic")
            .build();
        stack_header_bar.pack_end(&menu_button);

        NavigationPage::new(&stack_toolbar, "Instance")
    }
}
