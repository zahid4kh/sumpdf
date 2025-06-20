import type { Metadata } from "next";
import "./globals.css";
import { Analytics } from "@vercel/analytics/next";

export const metadata: Metadata = {
  title: "SumPDF",
  description:
    "SumPDF is your comprehensive, all-in-one PDF solution. Effortlessly combine multiple PDF files and convert various document types (SVG, PNG, JPG, JPEG, ODT, DOC, DOCX) to PDF. Enhance your PDF workflow by extracting specific pages, splitting files by range, deleting selected pages, reordering pages to your preference, and merging the remaining pages into a new document. All features are free and open-source.",
  icons: {
    icon: [
      {
        url: "/icons/sumpdf.ico",
        sizes: "any",
      },
      {
        url: "/icons/sumpdf.png",
        type: "image/png",
      },
    ],
  },
  manifest: "/manifest.json",
  applicationName: "SumPDF",
  keywords: [
    "PDF",
    "PDF merger",
    "PDF converter",
    "combine PDF",
    "convert to PDF",
    "extract PDF pages",
    "split PDF",
    "PDF splitter",
    "delete PDF pages",
    "remove PDF pages",
    "reorder PDF pages",
    "arrange PDF pages",
    "merge PDF pages",
    "PDF page management",
    "manage PDF pages",
    "PDF page editor",
    "document tools",
    "open source",
    "free PDF tool",
    "PDF utilities",
  ],
  authors: [{ name: "SumPDF" }],
  creator: "SumPDF",
  publisher: "SumPDF",
  formatDetection: {
    telephone: false,
    date: false,
    address: false,
    email: false,
    url: false,
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body>
        {children}
        <Analytics />
      </body>
    </html>
  );
}