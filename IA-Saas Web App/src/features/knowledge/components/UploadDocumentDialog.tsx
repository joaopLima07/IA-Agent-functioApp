import { useRef, useState } from "react";
import { Upload, FileUp, X } from "lucide-react";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { formatBytes } from "@/lib/format";
import { useUploadDocument } from "@/features/knowledge/hooks/useKnowledge";
import { cn } from "@/lib/utils";

export function UploadDocumentDialog() {
  const [open, setOpen] = useState(false);
  const [files, setFiles] = useState<File[]>([]);
  const [isDragging, setIsDragging] = useState(false);
  const inputRef = useRef<HTMLInputElement>(null);
  const upload = useUploadDocument();

  const addFiles = (fileList: FileList | null) => {
    if (!fileList) return;
    setFiles((prev) => [...prev, ...Array.from(fileList)]);
  };

  const handleUpload = async () => {
    for (const file of files) {
      await upload.mutateAsync({ name: file.name, size: file.size });
    }
    setFiles([]);
    setOpen(false);
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button className="gap-1.5">
          <Upload className="size-4" />
          Add document
        </Button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Add documents</DialogTitle>
          <DialogDescription>
            Upload files to your knowledge base. They'll be chunked and indexed for retrieval automatically.
          </DialogDescription>
        </DialogHeader>

        <div
          className={cn(
            "flex flex-col items-center justify-center gap-2 rounded-lg border-2 border-dashed border-border px-6 py-10 text-center transition-colors",
            isDragging && "border-primary bg-primary/5"
          )}
          onDragOver={(e) => {
            e.preventDefault();
            setIsDragging(true);
          }}
          onDragLeave={() => setIsDragging(false)}
          onDrop={(e) => {
            e.preventDefault();
            setIsDragging(false);
            addFiles(e.dataTransfer.files);
          }}
        >
          <FileUp className="size-6 text-muted-foreground" />
          <p className="text-sm text-muted-foreground">
            Drag and drop files here, or{" "}
            <button type="button" className="font-medium text-primary underline underline-offset-2" onClick={() => inputRef.current?.click()}>
              browse
            </button>
          </p>
          <p className="text-xs text-muted-foreground">PDF, DOCX, TXT, MD, CSV up to 25MB</p>
          <input
            ref={inputRef}
            type="file"
            multiple
            className="hidden"
            onChange={(e) => addFiles(e.target.files)}
            accept=".pdf,.docx,.doc,.txt,.md,.csv,.html"
          />
        </div>

        {files.length > 0 && (
          <ul className="max-h-40 space-y-1.5 overflow-y-auto">
            {files.map((file, i) => (
              <li key={`${file.name}-${i}`} className="flex items-center justify-between rounded-md border border-border px-3 py-2 text-sm">
                <div className="min-w-0">
                  <p className="truncate font-medium">{file.name}</p>
                  <p className="text-xs text-muted-foreground">{formatBytes(file.size)}</p>
                </div>
                <Button
                  variant="ghost"
                  size="icon-sm"
                  onClick={() => setFiles((prev) => prev.filter((_, idx) => idx !== i))}
                  aria-label={`Remove ${file.name}`}
                >
                  <X className="size-3.5" />
                </Button>
              </li>
            ))}
          </ul>
        )}

        <DialogFooter>
          <Button variant="outline" onClick={() => setOpen(false)}>
            Cancel
          </Button>
          <Button onClick={handleUpload} disabled={files.length === 0 || upload.isPending}>
            {upload.isPending ? "Uploading..." : `Upload ${files.length || ""}`.trim()}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
