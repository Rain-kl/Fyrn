import { useState } from "#app";
import { watch } from "vue";

export const WORKBENCH_MARKDOWN_THEMES = [
  "default",
  "github",
  "vuepress",
  "mk-cute",
  "smart-blue",
  "cyanosis",
] as const;

export type WorkbenchMarkdownTheme = (typeof WORKBENCH_MARKDOWN_THEMES)[number];

const STORAGE_KEY = "workbench.settings.markdownTheme";

export const useWorkbenchMarkdownTheme = () => {
  const theme = useState<WorkbenchMarkdownTheme>(
    "workbench-markdown-theme",
    () => "default",
  );
  const initialized = useState<boolean>(
    "workbench-markdown-theme-initialized",
    () => false,
  );

  if (process.client && !initialized.value) {
    const savedTheme = localStorage.getItem(STORAGE_KEY) as WorkbenchMarkdownTheme | null;
    if (savedTheme && WORKBENCH_MARKDOWN_THEMES.includes(savedTheme)) {
      theme.value = savedTheme;
    }

    watch(
      theme,
      (value) => {
        localStorage.setItem(STORAGE_KEY, value);
      },
      { flush: "post" },
    );

    initialized.value = true;
  }

  return theme;
};
