import { useState } from "react";
import { toast } from "sonner";
import { Check, Laptop, Moon, Sun } from "lucide-react";
import { PageHeader } from "@/components/common/PageHeader";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Card, CardContent, CardDescription, CardHeader, CardTitle, CardFooter } from "@/components/ui/card";
import { Label } from "@/components/ui/label";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Switch } from "@/components/ui/switch";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { useAuth } from "@/features/auth/hooks/useAuth";
import { useTheme, type ThemeMode } from "@/hooks/useTheme";
import { useLocalStorage } from "@/hooks/useLocalStorage";
import { initials } from "@/lib/format";
import { cn } from "@/lib/utils";

export default function SettingsPage() {
  const { user } = useAuth();
  const [name, setName] = useState(user?.name ?? "");
  const [email, setEmail] = useState(user?.email ?? "");
  const { theme, setTheme } = useTheme();
  const [preferences, setPreferences] = useLocalStorage("app-preferences", {
    notifications: true,
    sound: false,
    aiStreaming: true,
  });
  const [orgName, setOrgName] = useState("Northwind Retail");

  return (
    <div className="h-full overflow-y-auto scrollbar-thin">
      <div className="mx-auto max-w-3xl space-y-6 px-4 py-6 sm:px-6 lg:px-8">
        <PageHeader title="Settings" description="Manage your profile, appearance, and organization preferences." />

        <Tabs defaultValue="profile">
          <TabsList className="flex-wrap">
            <TabsTrigger value="profile">Profile</TabsTrigger>
            <TabsTrigger value="appearance">Appearance</TabsTrigger>
            <TabsTrigger value="preferences">Preferences</TabsTrigger>
            <TabsTrigger value="organization">Organization</TabsTrigger>
          </TabsList>

          <TabsContent value="profile">
            <Card>
              <CardHeader>
                <CardTitle>Profile</CardTitle>
                <CardDescription>Your personal account information.</CardDescription>
              </CardHeader>
              <CardContent className="space-y-5">
                <div className="flex items-center gap-4">
                  <Avatar className="size-14">
                    <AvatarFallback className="text-base">{initials(name || "You")}</AvatarFallback>
                  </Avatar>
                  <div>
                    <Button variant="outline" size="sm" onClick={() => toast.info("Avatar upload isn't available in this preview.")}>
                      Change avatar
                    </Button>
                    <p className="mt-1 text-xs text-muted-foreground">JPG, PNG, or GIF. Max 2MB.</p>
                  </div>
                </div>
                <div className="grid gap-2">
                  <Label htmlFor="profile-name">Name</Label>
                  <Input id="profile-name" value={name} onChange={(e) => setName(e.target.value)} />
                </div>
                <div className="grid gap-2">
                  <Label htmlFor="profile-email">Email</Label>
                  <Input id="profile-email" type="email" value={email} onChange={(e) => setEmail(e.target.value)} />
                </div>
              </CardContent>
              <CardFooter className="justify-end border-t border-border pt-5">
                <Button onClick={() => toast.success("Settings saved")}>Save changes</Button>
              </CardFooter>
            </Card>
          </TabsContent>

          <TabsContent value="appearance">
            <Card>
              <CardHeader>
                <CardTitle>Appearance</CardTitle>
                <CardDescription>Choose how Northwind AI looks on this device.</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="grid grid-cols-3 gap-3">
                  {(
                    [
                      { value: "light", label: "Light", icon: Sun },
                      { value: "dark", label: "Dark", icon: Moon },
                      { value: "system", label: "System", icon: Laptop },
                    ] as { value: ThemeMode; label: string; icon: typeof Sun }[]
                  ).map(({ value, label, icon: Icon }) => (
                    <button
                      key={value}
                      type="button"
                      onClick={() => setTheme(value)}
                      className={cn(
                        "relative flex flex-col items-center gap-2 rounded-lg border border-border p-4 text-sm font-medium transition-colors hover:border-primary/40",
                        theme === value && "border-primary bg-primary/5"
                      )}
                    >
                      {theme === value && (
                        <span className="absolute right-2 top-2 flex size-4 items-center justify-center rounded-full bg-primary text-primary-foreground">
                          <Check className="size-2.5" />
                        </span>
                      )}
                      <Icon className="size-5 text-muted-foreground" />
                      {label}
                    </button>
                  ))}
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="preferences">
            <Card>
              <CardHeader>
                <CardTitle>Preferences</CardTitle>
                <CardDescription>Control how the platform behaves while you work.</CardDescription>
              </CardHeader>
              <CardContent className="space-y-1">
                <div className="flex items-center justify-between py-3">
                  <div>
                    <p className="text-sm font-medium">Notifications</p>
                    <p className="text-sm text-muted-foreground">Receive updates about conversations and documents.</p>
                  </div>
                  <Switch
                    checked={preferences.notifications}
                    onCheckedChange={(checked) => setPreferences({ ...preferences, notifications: checked })}
                    aria-label="Toggle notifications"
                  />
                </div>
                <div className="flex items-center justify-between border-t border-border py-3">
                  <div>
                    <p className="text-sm font-medium">Sound</p>
                    <p className="text-sm text-muted-foreground">Play a sound when an agent finishes responding.</p>
                  </div>
                  <Switch
                    checked={preferences.sound}
                    onCheckedChange={(checked) => setPreferences({ ...preferences, sound: checked })}
                    aria-label="Toggle sound"
                  />
                </div>
                <div className="flex items-center justify-between border-t border-border py-3">
                  <div>
                    <p className="text-sm font-medium">AI streaming</p>
                    <p className="text-sm text-muted-foreground">Stream responses token-by-token instead of all at once.</p>
                  </div>
                  <Switch
                    checked={preferences.aiStreaming}
                    onCheckedChange={(checked) => setPreferences({ ...preferences, aiStreaming: checked })}
                    aria-label="Toggle AI streaming"
                  />
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="organization">
            <Card>
              <CardHeader>
                <CardTitle>Organization</CardTitle>
                <CardDescription>Details shared across your organization's workspace.</CardDescription>
              </CardHeader>
              <CardContent className="space-y-5">
                <div className="flex items-center gap-4">
                  <Avatar className="size-14 rounded-lg">
                    <AvatarFallback className="rounded-lg text-base">{initials(orgName)}</AvatarFallback>
                  </Avatar>
                  <div>
                    <Button variant="outline" size="sm" onClick={() => toast.info("Logo upload isn't available in this preview.")}>
                      Change logo
                    </Button>
                    <p className="mt-1 text-xs text-muted-foreground">SVG or PNG, square recommended.</p>
                  </div>
                </div>
                <div className="grid gap-2">
                  <Label htmlFor="org-name">Company name</Label>
                  <Input id="org-name" value={orgName} onChange={(e) => setOrgName(e.target.value)} />
                </div>
              </CardContent>
              <CardFooter className="justify-end border-t border-border pt-5">
                <Button onClick={() => toast.success("Organization settings saved")}>Save changes</Button>
              </CardFooter>
            </Card>
          </TabsContent>
        </Tabs>
      </div>
    </div>
  );
}
